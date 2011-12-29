package org.ksa14.webhard.sftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpTransfer {
	public static final boolean TRANSFER_UP = true;
	public static final boolean TRANSFER_DOWN = false;

	public static final int MODE_NONE = 0;
	public static final int MODE_STARTED = 1;
	public static final int MODE_RUNNING = 2;
	public static final int MODE_PAUSED = 3;
	public static final int MODE_STOPPED = 4;
	public static final int MODE_FINISHED = 5;
	
	public static final int MAX_TRANSFER = 5;
	
	public static final int OVERWRITE_ASK = 0;
	public static final int OVERWRITE_YES = 1;
	public static final int OVERWRITE_RESUME = 2;
	public static final String[] OVERWRITE_EOPTION = {"건너뛰기", "덮어쓰기", "모두 덮어쓰기"};
	public static final String[] OVERWRITE_ROPTION = {"건너뛰기", "덮어쓰기", "모두 덮어쓰기", "이어받기", "모두 이어받기"};
	
	public static class SftpTransferData {
		public String pathSrc;
		public String pathDest;
		public String fileName;
		
		public boolean isDir;
		public boolean isUp;

		public int mode = MODE_NONE;
		public boolean overwrite = true;
		
		public long fileSize = 0;
		public long fileSizeDone = 0;
		public float fileSpeed = 0;

		public long prevTime = 0;
		public long prevDone = 0;

		public Thread thread = null;
		
		public SftpTransferData(String src, String dest, String file, boolean dir, boolean up) {
			pathSrc = src;
			pathDest = dest;
			fileName = file;
			
			isDir = dir;
			isUp = up;
		}
		
		public String getSrcName() {
			return pathSrc + (isUp ? File.separator : "/") + fileName;
		}
		
		public String getDestName() {
			return pathDest + (isUp ? "/" : File.separator) + fileName;
		}
		
		public String getDestTempName() {
			return pathDest + (isUp? "/" : File.separator) + "_ksa14webhard_" + fileName;
		}
	}
	
	public static void download(Vector<SftpTransferData> filelist) {
		try {
			int i;
			
			i = 0;
			while (i < filelist.size()) {
				SftpTransferData fileitem = filelist.elementAt(i);
				
				if (fileitem.isDir) {
					ChannelSftp channel = SftpAdapter.getMainChannel();
					
					Vector<?> lslist = channel.ls(fileitem.getSrcName());
					Iterator<?> lsiter = lslist.iterator();
					while (lsiter.hasNext()) {
						LsEntry lsentry = (LsEntry)lsiter.next();
						String filename = lsentry.getFilename();
						if ((filename.charAt(0) != '.') && !filename.equals("recycle_bin"))
							filelist.add(new SftpTransferData(fileitem.getSrcName(), fileitem.getDestName(), filename, lsentry.getAttrs().isDir(), TRANSFER_DOWN));
					}
					
					filelist.remove(i);
					continue;
				}
				
				i++;
			}
			
			int eall = OVERWRITE_ASK, rall = OVERWRITE_ASK;
			int eask, rask;
			i = 0;
			while (i < filelist.size()) {
				final SftpTransferData fileitem = filelist.elementAt(i);
				
				if (fileitem.fileName.substring(0, 14).equals("_ksa14webhard_")) {
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "\"" + fileitem.fileName + "\" 파일은 업로드 중인 파일이므로 다운로드할 수 없습니다.");
					filelist.remove(i);
					continue;
				}
				
				File fileexist = new File(fileitem.getDestName());
				File filetemp = new File(fileitem.getDestTempName());
				File filedir = fileexist.getParentFile();
				
				if (!(filedir.isDirectory() || filedir.mkdirs())) {
					throw new Exception();
				}
				
				if (fileexist.isFile()) {
					eask = 0;
					
					if (eall == OVERWRITE_ASK)
						eask = JOptionPane.showOptionDialog(
								null,
								"\"" + fileitem.fileName + "\" 파일이 존재합니다. 덮어쓰시겠습니까?",
								"파일 덮어쓰기",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								OVERWRITE_EOPTION,
								OVERWRITE_EOPTION[0]);
					else if (eall == OVERWRITE_YES)
						eask = 1;
					
					if (eask == 1) {
						fileexist.delete();
						fileitem.overwrite = true;
					} else if (eask == 2) {
						fileexist.delete();
						fileitem.overwrite = true;
						eall = OVERWRITE_YES;
					} else {
						filelist.remove(i);
						continue;
					}
				} else if (filetemp.isFile()) {
					rask = 0;
					
					if (rall == OVERWRITE_ASK)
						rask = JOptionPane.showOptionDialog(
								null,
								"전송중이던 \"" + fileitem.fileName + "\" 파일이 존재합니다. 덮어쓰시겠습니까?",
								"파일 덮어쓰기",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								OVERWRITE_ROPTION,
								OVERWRITE_ROPTION[0]);
					else if (rall == OVERWRITE_YES)
						rask = 1;
					else if (rall == OVERWRITE_RESUME)
						rask = 3;

					
					if (rask == 1) {
						fileexist.delete();
						fileitem.overwrite = true;
					} else if (rask == 2) {
						fileitem.overwrite = true;
						eall = OVERWRITE_YES;
					} else if (rask == 3) {
						fileitem.overwrite = false;
					} else if (rask == 4) {
						fileitem.overwrite = true;
						eall = OVERWRITE_RESUME;
					} else {
						filelist.remove(i);
						continue;
					}
				}
				
				fileitem.thread = getDownloadThread(fileitem, fileitem.overwrite);
				
				i++;
			}
		} catch (Exception e) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "다운로드에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "다운로드에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_FAIL, null);
			return;
		}

		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "다운로드 시작");
		MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_START, filelist);
	}
	
	public static Thread getDownloadThread(final SftpTransferData fileitem, final boolean ow) {
		return (new Thread() {
			public void run() {
				try {
					ChannelSftp channel = SftpAdapter.getNewChannel();
					File destfile = new File(fileitem.getDestTempName());
					File destdir = destfile.getParentFile();
					if (!destdir.isDirectory() && !destdir.mkdirs())
						throw new Exception();
					
					byte[] inbuf = new byte[1024];
					long sftpskip = ow ? 0 : destfile.length();
					fileitem.fileSizeDone = sftpskip;
					BufferedOutputStream fileout = new BufferedOutputStream(new FileOutputStream(destfile, !ow));
					BufferedInputStream sftpin = new BufferedInputStream(channel.get(fileitem.getSrcName(), new SftpTransferMonitor(fileitem), sftpskip));
					
					while (sftpin.read(inbuf, 0, 1024) >= 0) {
						fileout.write(inbuf);
						fileout.flush();
					}
					
					fileout.close();							
					if (fileitem.mode == MODE_FINISHED) {
						File filetemp = new File(fileitem.getDestTempName());
						filetemp.renameTo(new File(fileitem.getDestName()));
					}
				} catch (Exception e) {
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "다운로드에 실패했습니다");
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "다운로드에 실패했습니다");
					MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_FAIL, fileitem);
				}
				
			}
		});
	}
	
	public static void upload(Vector<SftpTransferData> filelist) {
		try {
			int i;
			
			i = 0;
			while (i < filelist.size()) {
				SftpTransferData fileitem = filelist.elementAt(i);
				
				if (fileitem.isDir) {
					File fileup = new File(fileitem.getSrcName());
					File[] dirlist = fileup.listFiles();
					for (File filechild : dirlist)
						filelist.add(new SftpTransferData(fileup.getPath(), fileitem.getDestName(), filechild.getName(), filechild.isDirectory(), TRANSFER_UP));
					filelist.remove(i);
					continue;
				}
				
				i++;
			}
			
			i = 0;
			while (i < filelist.size()) {
				i++;
			}
		} catch (Exception e) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "업로드에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "업로드에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.UPLOAD_FAIL, null);
			return;
		}

		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "업로드 시작");
		MsgBroadcaster.broadcastMsg(MsgListener.UPLOAD_START, filelist);
	}
}
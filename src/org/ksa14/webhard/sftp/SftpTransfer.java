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
	public static final String[] OVERWRITE_OPTION = {"건너뛰기", "덮어쓰기", "모두 덮어쓰기", "이어받기", "모두 이어받기"};
	
	public static class SftpTransferData {
		public String source;
		public String destination;
		public String fileName;
		public boolean isDir;
		public long fileSize = 0;
		public long fileSizeDone = 0;
		public float fileSpeed = 0;
		public long prevTime = 0;
		public long prevDone = 0;
		public int mode = MODE_NONE;
		public boolean overwrite = true;
		public Thread thread = null;
		
		public SftpTransferData(String src, String dest, String file, boolean dir, boolean up) {
			if (up) {
				source = src + "\\" + file;
				destination = dest + "/" + file;
			} else {
				source = src + "/" + file;
				destination = dest + "\\" + file;
			}
			fileName = file;
			isDir = dir;
			mode = MODE_NONE;
		}
	}
	
	public static void download(Vector<SftpTransferData> filelist) {
		try {
			int i = 0;
			while (i < filelist.size()) {
				SftpTransferData fileitem = filelist.elementAt(i);
				
				if (fileitem.isDir) {
					ChannelSftp channel = SftpAdapter.getMainChannel();
					
					Vector<?> lslist = channel.ls(fileitem.source);
					Iterator<?> lsiter = lslist.iterator();
					while (lsiter.hasNext()) {
						LsEntry lsentry = (LsEntry)lsiter.next();
						String filename = lsentry.getFilename();
						if ((filename.charAt(0) != '.') && !filename.equals("recycle_bin"))
							filelist.add(new SftpTransferData(fileitem.source, fileitem.destination, filename, lsentry.getAttrs().isDir(), TRANSFER_DOWN));
					}
					
					filelist.remove(i);
					continue;
				}
				
				i++;
			}
			
			int owall = OVERWRITE_ASK;
			int owask;			
			i = 0;
			while (i < filelist.size()) {
				final SftpTransferData fileitem = filelist.elementAt(i);
				
				File fileexist = new File(fileitem.destination);
				File filedir = fileexist.getParentFile();
				
				if (!filedir.isDirectory()) {
					if (!filedir.mkdirs())
						throw new Exception();
				}
				
				owask = 1;
				if (fileexist.isFile()) {
					if (owall == OVERWRITE_ASK)
						owask = JOptionPane.showOptionDialog(null, "\"" + fileitem.fileName + "\" 이 존재합니다. 덮어쓰시겠습니까?", "파일 덮어쓰기", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, OVERWRITE_OPTION, OVERWRITE_OPTION[0]);
					else if (owall == OVERWRITE_RESUME)
						owask = 3;
				}
				switch (owask) {
				case 1:
					fileitem.overwrite = true;
					break;
				case 2:
					fileitem.overwrite = true;
					owall = OVERWRITE_YES;
					break;
				case 3:
					fileitem.overwrite = false;
					break;
				case 4:
					fileitem.overwrite = false;
					owall = OVERWRITE_RESUME;
					break;
				default:					
					filelist.remove(i);
					continue;
				}
				
				fileitem.thread = new Thread() {
					public void run() {
						try {
							ChannelSftp channel = SftpAdapter.getNewChannel();
							File destfile = new File(fileitem.destination);
							File destdir = destfile.getParentFile();
							if (!destdir.isDirectory() && !destdir.mkdirs())
								throw new Exception();
							
							byte[] inbuf = new byte[1024];
							long sftpskip = fileitem.overwrite ? 0 : destfile.length();
							fileitem.fileSizeDone = sftpskip;
							BufferedOutputStream fileout = new BufferedOutputStream(new FileOutputStream(new File(fileitem.destination), !fileitem.overwrite));
							BufferedInputStream sftpin = new BufferedInputStream(channel.get(fileitem.source, new SftpTransferMonitor(fileitem), sftpskip));
							
							while (sftpin.read(inbuf, 0, 1024) >= 0) {
								fileout.write(inbuf);
								fileout.flush();
							}
							
							fileout.close();
						} catch (Exception e) {
							MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "다운로드에 실패했습니다");
							MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "다운로드에 실패했습니다");
							MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_FAIL, fileitem);
						}
					}
				};
				
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
}
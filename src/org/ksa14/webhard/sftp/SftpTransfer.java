package org.ksa14.webhard.sftp;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpTransfer {
	public static final int MODE_OVERWRITE	= ChannelSftp.OVERWRITE;
	public static final int MODE_RESUME		= ChannelSftp.RESUME;
	public static final int MODE_ASK			= 2;
	public static final int MODE_PASS			= 3;
	public static final int MODE_RUNNING		= 4;
	public static final int MODE_PAUSE		= 5;
	public static final int MODE_FINISH		= 6;
	
	public static final String[] MODE_OPTION	= {"덮어쓰기", "모두 덮어쓰기", "이어서 받기", "모두 이어서 받기", "무시하기"};
	
	public static int modeall = MODE_ASK;
	
	public static class TransferFileData {
		public String SourcePath;
		public String FileName;
		public String Destination;
		public boolean IsDir;
		public long Size;
		public long Count;
		public float Speed;
		public int Mode;
		
		public TransferFileData(String s, String f, String d, boolean i) {
			SourcePath = s;
			FileName = f;
			Destination = d;
			IsDir = i;
			Size = 0;
			Count = 0;
		}
	}
	
	public static void Download(Vector<TransferFileData> data, boolean remode) {
		Iterator<TransferFileData> datai = data.iterator();
		
		if (remode)
			modeall = MODE_ASK;

		try {
			while (datai.hasNext()) {
				TransferFileData filedata = datai.next();
				
				String srcpath = filedata.SourcePath + "/" + filedata.FileName;
				String destpath = filedata.Destination + "/" + filedata.FileName;
				
				File fileitem = new File(destpath);
				
				if (filedata.IsDir) {
					ChannelSftp channel = SftpAdapter.GetNewChannel();
					if ((channel == null) || !channel.isConnected()) 
						throw new Exception();
					
					if (!fileitem.isDirectory()) {
						if (!fileitem.mkdir())
							throw new Exception();
					}
					Vector<?> direntry = (Vector<?>)channel.ls(srcpath);
					Iterator<?> diriter = direntry.iterator();
					Vector<TransferFileData> dirdata = new Vector<TransferFileData>();
					while (diriter.hasNext()) {
						LsEntry subfileentry = (LsEntry)diriter.next();
						String subfilename = subfileentry.getFilename();
						if (subfilename.charAt(0) != '.')
							dirdata.add(new TransferFileData(srcpath, subfilename, destpath, subfileentry.getAttrs().isDir()));
					}
					
					channel.disconnect();
					
					Download(dirdata, false);
				} else {
					int mode = MODE_OVERWRITE;
					
					if ((modeall == MODE_ASK) && fileitem.isFile()) {
						int modeask = JOptionPane.showOptionDialog(null, "\"" + filedata.FileName + "\" 파일이 존재합니다\n덮어쓰시겠습니까?", "파일 덮어쓰기", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, MODE_OPTION, MODE_OPTION[0]);
						switch (modeask) {
						case 0:
							mode = MODE_OVERWRITE;
							break;
						case 1:
							modeall = MODE_OVERWRITE;
							mode = MODE_OVERWRITE;
							break;
						case 2:
							mode = MODE_RESUME;
							break;
						case 3:
							modeall = MODE_RESUME;
							mode = MODE_RESUME;
							break;
						default:
							mode = MODE_PASS;
						}
					}
					
					if ((mode != MODE_ASK) && (mode != MODE_PASS)) {
						filedata.Mode = mode;						
						MsgBroadcaster.BroadcastMsg(MsgListener.DOWNLOAD_START, filedata);
					}
				}
			}
		} catch (Exception e) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 다운로드 중 문제가 발생했습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.DOWNLOAD_FAIL, "파일 다운로드 중 문제가 발생했습니다");
			return;
		}
	}
	
	public static void Upload() {
		
	}
}

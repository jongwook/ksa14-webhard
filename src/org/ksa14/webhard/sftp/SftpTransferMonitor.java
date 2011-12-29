package org.ksa14.webhard.sftp;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;

import com.jcraft.jsch.SftpProgressMonitor;

public class SftpTransferMonitor implements SftpProgressMonitor {
	SftpTransferData fileData;
	
	public SftpTransferMonitor(SftpTransferData file) {
		fileData = file;
	}
	
	public void init(int op, String src, String dest, long max) {
		fileData.mode = SftpTransfer.MODE_RUNNING;
		fileData.fileSize = max;
		fileData.prevTime = System.currentTimeMillis();
	}
	
	public boolean count(long count) {
		if (fileData.mode == SftpTransfer.MODE_RUNNING)
			fileData.fileSizeDone += count;
		else
			return false;
		
		long ctime = System.currentTimeMillis();
		if ((ctime - fileData.prevTime) >= 500) {
			fileData.fileSpeed = (fileData.fileSizeDone - fileData.prevDone) / ((ctime - fileData.prevTime) * 0.001f);
			
			fileData.prevDone = fileData.fileSizeDone;
			fileData.prevTime = ctime;
			
			MsgBroadcaster.broadcastMsg(fileData.upload ? MsgListener.UPLOAD_UPDATE : MsgListener.DOWNLOAD_UPDATE, fileData);
		}
		
		if ((fileData.mode == SftpTransfer.MODE_STOPPED) || (fileData.mode == SftpTransfer.MODE_PAUSED))
			return false;
		
		return true;
	}

	public void end() {
		if (fileData.fileSize <= fileData.fileSizeDone) {
			fileData.mode = SftpTransfer.MODE_FINISHED;
			
			if (fileData.upload == SftpTransfer.TRANSFER_DOWN) {
			}
		}
		MsgBroadcaster.broadcastMsg(fileData.upload ? MsgListener.UPLOAD_UPDATE : MsgListener.DOWNLOAD_UPDATE, fileData);
	}
}

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
		
		long ctime = System.currentTimeMillis();
		if ((ctime - fileData.prevTime) >= 500) {
			fileData.fileSpeed = (fileData.fileSizeDone - fileData.prevDone) / ((ctime - fileData.prevTime) * 0.001f);
			
			fileData.prevDone = fileData.fileSizeDone;
			fileData.prevTime = ctime;
			
			MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_UPDATE, fileData);
		}
		
		return true;
	}

	public void end() {
		fileData.mode = SftpTransfer.MODE_FINISHED;
		MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_DONE, fileData);
	}
}

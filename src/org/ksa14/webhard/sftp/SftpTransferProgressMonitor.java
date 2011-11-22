package org.ksa14.webhard.sftp;

import org.ksa14.webhard.sftp.SftpTransfer.TransferFileData;

import com.jcraft.jsch.SftpProgressMonitor;

public class SftpTransferProgressMonitor implements SftpProgressMonitor {
	TransferFileData TransferFile;
	long StartTime;
	
	public SftpTransferProgressMonitor(TransferFileData data) {
		TransferFile = data;
	}

	public void init(int op, String src, String dest, long max) {
		TransferFile.Size = max;
		StartTime = System.currentTimeMillis();
	}

	@Override
	public boolean count(long count) {
		long ThisTime = System.currentTimeMillis();
		TransferFile.Count += count;
		TransferFile.Speed = TransferFile.Count / ((ThisTime - StartTime) * 0.001f);
		return true;
	}

	@Override
	public void end() {
		TransferFile.Mode = SftpTransfer.MODE_FINISH;
	}
}

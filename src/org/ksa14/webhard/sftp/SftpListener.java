package org.ksa14.webhard.sftp;

public interface SftpListener {
	public static final int SFTP_INFO = 0;
	public static final int SFTP_SUCCEED = 1;
	public static final int SFTP_FAILED = -1;
	public void UpdateStatus(final String status, final int arg);
}

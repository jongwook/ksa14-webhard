package org.ksa14.webhard.sftp;

public interface SftpListener {
	public static final int INFO = 0;
	public static final int SUCCEED = 1;
	public static final int FAILED = -1;
	
	public static final int DIRLIST_DONE = 2;
	public static final int FILELIST_DONE = 3;
	
	public void UpdateStatus(final int type, final Object arg);
}

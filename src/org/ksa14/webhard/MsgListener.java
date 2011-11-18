package org.ksa14.webhard;

public interface MsgListener {
	public static final int STATUS_INFO		= 0;
	
	public static final int CONNECT_NONE		= 1;
	public static final int CONNECT_SUCCESS	= 2;
	public static final int CONNECT_FAIL		= 3;
	
	public static final int DIRTREE_DONE		= 4;
	public static final int DIRTREE_FAIL		= 5;
	
	public static final int FILELIST_DONE		= 6;
	public static final int FILELIST_FAIL		= 7;
	
	public void ReceiveMsg(final int type, final Object arg);
}

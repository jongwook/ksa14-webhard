package org.ksa14.webhard;

public interface MsgListener {
	public static final int STATUS_INFO		= 0;
	
	public static final int CONNECT_NONE		= 1;
	public static final int CONNECT_FAIL		= 2;
	public static final int CONNECT_SUCCESS	= 3;
	
	public static final int PANEL_EXPLORE		= 4;
	public static final int PANEL_SEARCH		= 5;
	
	public static final int DIRTREE_FAIL		= 6;
	public static final int DIRTREE_DONE		= 7;

	public static final int FILELIST_FAIL		= 8;
	public static final int FILELIST_DONE		= 9;
	
	public static final int SEARCH_FAIL		= 10;
	public static final int SEARCH_DONE		= 11;
	
	public static final int FILE_DOWN_FAIL	= 12;
	
	public void ReceiveMsg(final int type, final Object arg);
}

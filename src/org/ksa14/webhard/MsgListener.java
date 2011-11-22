package org.ksa14.webhard;

public interface MsgListener {
	public static final int STATUS_INFO		= 0;
	
	public static final int CONNECT_NONE		= 1;
	public static final int CONNECT_FAIL		= 2;
	public static final int CONNECT_SUCCESS	= 3;
	
	public static final int PANEL_EXPLORE		= 4;
	public static final int PANEL_SEARCH		= 5;
	public static final int PANEL_TRANSFER	= 6;
	public static final int PANEL_DOWNLOAD	= 7;
	public static final int PANEL_UPLOAD		= 8;
	
	public static final int DIRTREE_FAIL		= 9;
	public static final int DIRTREE_DONE		= 10;

	public static final int FILELIST_FAIL		= 11;
	public static final int FILELIST_DONE		= 12;
	
	public static final int SEARCH_FAIL		= 13;
	public static final int SEARCH_DONE		= 14;
	
	public static final int DOWNLOAD_CLICK	= 15;
	public static final int DOWNLOAD_START	= 16;
	public static final int DOWNLOAD_PAUSE	= 17;
	public static final int DOWNLOAD_RESUME	= 18;
	public static final int DOWNLOAD_STOP		= 19;
	public static final int DOWNLOAD_FAIL		= 20;
	public static final int DOWNLOAD_DONE		= 21;
	
	public static final int UPLOAD_CLICK		= 22;
	public static final int UPLOAD_START		= 23;
	public static final int UPLOAD_PAUSE		= 24;
	public static final int UPLOAD_RESUME		= 25;
	public static final int UPLOAD_STOP		= 26;
	public static final int UPLOAD_FAIL		= 27;
	public static final int UPLOAD_DONE		= 28;
	
	public void ReceiveMsg(final int type, final Object arg);
}

package org.ksa14.webhard;

/**
 * Message listener of classes
 * 
 * @author Jongwook, ThomasJun
 */
public interface MsgListener {
	// Message types
	public static final int STATUS_INFO		= 1;
	public static final int STATUS_MESSAGE	= 2;
	
	public static final int PANEL_EXPLORE		= 11;
	public static final int PANEL_SEARCH		= 12;
	public static final int PANEL_TRANSFER	= 13;
	public static final int PANEL_DOWNLOAD	= 14;
	public static final int PANEL_UPLOAD		= 15;
	
	public static final int CONNECT_FAIL		= 21;
	public static final int CONNECT_SUCCESS	= 22;
	
	public static final int DIRTREE_FAIL		= 31;
	public static final int DIRTREE_DONE		= 32;

	public static final int FILELIST_FAIL		= 41;
	public static final int FILELIST_DONE		= 42;
	
	public static final int SEARCH_FAIL		= 51;
	public static final int SEARCH_DONE		= 52;
	
	public static final int DOWNLOAD_CLICK	= 61;
	public static final int DOWNLOAD_START	= 62;
	public static final int DOWNLOAD_FAIL		= 63;
	public static final int DOWNLOAD_DONE		= 64;
	public static final int DOWNLOAD_UPDATE	= 65;
	
	public static final int UPLOAD_CLICK		= 71;
	public static final int UPLOAD_START		= 72;
	public static final int UPLOAD_FAIL		= 73;
	public static final int UPLOAD_DONE		= 74;
	
	public void receiveMsg(final int type, final Object arg);
}

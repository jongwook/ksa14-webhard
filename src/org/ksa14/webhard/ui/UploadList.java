package org.ksa14.webhard.ui;

import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

public class UploadList extends TransferList implements MsgListener {
	private static final long serialVersionUID = 0L;
	
	private static UploadList TheInstance;
	
	private UploadList() {
		super();
		
		MsgBroadcaster.AddListener(this);
	}

	public static UploadList GetInstance() {
		return (TheInstance == null) ? TheInstance = new UploadList() : TheInstance;
	}

	public void ReceiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
			}
		});
	}
}

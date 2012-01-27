package org.ksa14.webhard.ui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;

public class DownloadList extends TransferList implements MsgListener {
	private static final long serialVersionUID = 0L;
	
	private static DownloadList theInstance;
	
	private DownloadList() {
		super();
		
		MsgBroadcaster.addListener(this);
	}

	public static DownloadList getInstance() {
		return (theInstance == null) ? theInstance = new DownloadList() : theInstance;
	}
	
	public void pauseTransfer(int row) {
		final SftpTransferData filedata = fileTransfer.get(row);
		
		filedata.mode = SftpTransfer.MODE_PAUSED;
		filedata.thread = SftpTransfer.getDownloadThread(filedata, false);
	}
	
	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.DOWNLOAD_START) {
					Iterator<?> fileiter = ((Vector<?>)arg).iterator();
					while (fileiter.hasNext()) {
						SftpTransferData filedata = (SftpTransferData)fileiter.next();
						fileTransfer.add(filedata);
						((DefaultTableModel)getModel()).addRow(new Object[] {filedata.fileName, "", "", "", "", "", ""});
						updateList(filedata);
					}
				}
				
				if (type == MsgListener.DOWNLOAD_UPDATE)
					updateList((SftpTransferData)arg);
				
				if (type == MsgListener.DOWNLOAD_FAIL) {
					((SftpTransferData)arg).mode = SftpTransfer.MODE_STOPPED;
					updateList((SftpTransferData)arg);
				}
			}
		});
	}
}

package org.ksa14.webhard.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpAdapter;
import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;
import org.ksa14.webhard.sftp.SftpTransferMonitor;

import com.jcraft.jsch.ChannelSftp;

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
		filedata.thread = new Thread() {
			public void run() {
				try {
					ChannelSftp channel = SftpAdapter.getNewChannel();
					byte[] inbuf = new byte[1024];
					BufferedOutputStream fileout = new BufferedOutputStream(new FileOutputStream(new File(filedata.destination), true));
					BufferedInputStream sftpin = new BufferedInputStream(channel.get(filedata.source, new SftpTransferMonitor(filedata), filedata.fileSizeDone));

					while (sftpin.read(inbuf, 0, 1024) >= 0) {
						fileout.write(inbuf);
						fileout.flush();
					}
					
					fileout.close();
				} catch (Exception e) {
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "다운로드에 실패했습니다");
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "다운로드에 실패했습니다");
					MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_FAIL, filedata);
				}
			}
		};
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
			}
		});
	}
}

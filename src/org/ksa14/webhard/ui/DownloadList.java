package org.ksa14.webhard.ui;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpAdapter;
import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.TransferFileData;
import org.ksa14.webhard.sftp.SftpTransferProgressMonitor;

import com.jcraft.jsch.ChannelSftp;

public class DownloadList extends TransferList implements MsgListener {
	private static final long serialVersionUID = 0L;
	
	private static DownloadList TheInstance;
	
	private DownloadList() {
		super();
		
		MsgBroadcaster.AddListener(this);
	}

	public static DownloadList GetInstance() {
		return (TheInstance == null) ? TheInstance = new DownloadList() : TheInstance;
	}

	public void ReceiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if ((type == MsgListener.DOWNLOAD_START)) {
					final TransferFileData filedata = (TransferFileData)arg;
					
					((DefaultTableModel)getModel()).addRow(new Object[] {filedata.FileName, "", "", "", "", "pause", ""});
					TransferFile.add(filedata);
					new Thread() {
						public void run() {
							try {
								ChannelSftp channel = SftpAdapter.GetNewChannel();
								if ((channel == null) || !channel.isConnected()) 
									throw new Exception();
								channel.get(filedata.SourcePath + "/" + filedata.FileName, filedata.Destination + "\\" + filedata.FileName, new SftpTransferProgressMonitor(filedata), filedata.Mode);
							} catch (Exception e) {
								MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 다운로드 중 문제가 발생했습니다");
								MsgBroadcaster.BroadcastMsg(MsgListener.DOWNLOAD_FAIL, "파일 다운로드 중 문제가 발생했습니다");
								return;
							}
						}
					}.start();
					
					filedata.Mode = SftpTransfer.MODE_RUNNING;
				}
			}
		});
	}
}

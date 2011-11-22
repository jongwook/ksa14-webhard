package org.ksa14.webhard.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.TransferFileData;

/**
 * WebhardPanel maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel implements MsgListener {
	public static final long serialVersionUID = 0L;
	
	public static final int FILEPANE_EXPLORE = 0;
	public static final int FILEPANE_SEARCH = 1;
	public static final int FILEPANE_TRANSFER = 2;

	private StatusBarLabel Status;
	private JToolBar ToolsBar;
	private JPanel FilePanel;
	private JSplitPane ExplorePane;
	private JScrollPane SearchPane;
	private JTabbedPane TransferPane;
	private DirectoryTree ExploreDir;
	private ExploreFileList ExploreFile;
	private SearchFileList SearchFile;
	private DownloadList DownloadFile;
	private UploadList UploadFile;

	/**
	 * Initializes the GUI components of the main webhard window
	 */
	public WebhardPanel() {
		setBackground(Color.lightGray);
		
		// Set layout
		setLayout(new BorderLayout());
		
		// Initialize the status bar
		Status = new StatusBarLabel("준비 중");
		add(Status, BorderLayout.SOUTH);

		// Initialize the tool bar
		ToolsBar = new WebhardToolBar();
		add(ToolsBar, BorderLayout.NORTH);

		// Initialize the directory tree and explore file list 
		ExploreDir = DirectoryTree.GetInstance();
		JScrollPane spedir = new JScrollPane(ExploreDir);
		spedir.setPreferredSize(new Dimension(200, 600));
		
		ExploreFile = ExploreFileList.GetInstance();
		JScrollPane spefile = new JScrollPane(ExploreFile);
		spefile.getViewport().setBackground(Color.white);
		
		ExplorePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spedir, spefile);
		
		// Initialize the search file list
		SearchFile = SearchFileList.GetInstance();
		SearchPane = new JScrollPane(SearchFile);
		SearchPane.getViewport().setBackground(Color.white);
		
		// Initialize the transfer list
		DownloadFile = DownloadList.GetInstance();
		JScrollPane spdfile = new JScrollPane(DownloadFile);
		spdfile.getViewport().setBackground(Color.white);
		
		UploadFile = UploadList.GetInstance();
		JScrollPane spufile = new JScrollPane(UploadFile);
		spufile.getViewport().setBackground(Color.white);
		
		TransferPane = new JTabbedPane(JTabbedPane.TOP);
		TransferPane.addTab("다운로드", null, spdfile);
		TransferPane.addTab("업로드", null, spufile);
		
		// Initialize file panel
		FilePanel = new JPanel();
		FilePanel.setLayout(new CardLayout());
		
		FilePanel.add(ExplorePane, "explore");
		FilePanel.add(SearchPane, "search");
		FilePanel.add(TransferPane, "transfer");

		add(FilePanel, BorderLayout.CENTER);
		
		// Add message listener to broadcaster
		MsgBroadcaster.AddListener(this);
		
		ExploreDir.UpdateTree();
	}
	
	private int GetFilePanelIndex() {
		Component c[] = FilePanel.getComponents();
		int i = 0;
		while (i < c.length) {
			if (c[i].isVisible())
				return i;
			i++;
		}
		return -1;
	}

	public void ReceiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.CONNECT_NONE) {
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
					ExploreDir.setEnabled(false);
					ExploreFile.setEnabled(false);
					SearchFile.setEnabled(false);
				}
				
				if ((type == MsgListener.PANEL_EXPLORE))
					((CardLayout)FilePanel.getLayout()).show(FilePanel, "explore");
				
				if ((type == MsgListener.PANEL_SEARCH))
					((CardLayout)FilePanel.getLayout()).show(FilePanel, "search");
				
				if ((type == MsgListener.PANEL_TRANSFER))
					((CardLayout)FilePanel.getLayout()).show(FilePanel, "transfer");
				
				if ((type == MsgListener.PANEL_DOWNLOAD)) {
					((CardLayout)FilePanel.getLayout()).show(FilePanel, "transfer");
					TransferPane.setSelectedIndex(0);
				}
				
				if ((type == MsgListener.PANEL_UPLOAD)) {
					((CardLayout)FilePanel.getLayout()).show(FilePanel, "transfer");
					TransferPane.setSelectedIndex(1);
				}
				
				if ((type == MsgListener.DOWNLOAD_CLICK)) {
					int fpi = GetFilePanelIndex();
					
					if (fpi == FILEPANE_EXPLORE) {
						int[] srows = ExploreFile.getSelectedRows();
						if (srows.length > 0) {
							JFileChooser savefile = new JFileChooser();
							savefile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							savefile.setAcceptAllFileFilterUsed(false);
							savefile.setDialogTitle("저장 위치");
							if (savefile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								String srcpath = ExploreDir.GetLastPathString();
								if (srcpath.equals("/"))
									srcpath = "";
								String destpath = savefile.getSelectedFile().toString();
								if (destpath.charAt(destpath.length() - 1) == '\\')
									destpath = destpath.substring(0, destpath.length() - 1);
								
								final Vector<TransferFileData> filelist = new Vector<TransferFileData>();
								for (int sr : srows) {
									String filename = (String)ExploreFile.getValueAt(sr, FileList.COLUMN_FILENAME);
									boolean isdir = ExploreFile.getValueAt(sr, FileList.COLUMN_EXT).equals(".");
									filelist.add(new TransferFileData(srcpath, filename, destpath, isdir));
								}
								
								new Thread() {
									public void run() {
										SftpTransfer.Download(filelist, true);
									}
								}.start();
								
								((CardLayout)FilePanel.getLayout()).show(FilePanel, "transfer");
								TransferPane.setSelectedIndex(0);
							}
						}
					}
				}
				/*
				if ((type == MsgListener.TRANSFER_UP_START)) {
					int fpi = GetFilePanelIndex();
					if ((fpi == FILEPANE_EXPLORE) || (fpi == FILEPANE_SEARCH)) {
						((CardLayout)FilePanel.getLayout()).show(FilePanel, "transfer");
						TransferPane.setSelectedIndex(1);
					}
				}*/
				
				if ((type == MsgListener.DIRTREE_FAIL) || (type == MsgListener.FILELIST_FAIL) || (type == MsgListener.SEARCH_FAIL) || (type == MsgListener.DOWNLOAD_FAIL) || (type == MsgListener.UPLOAD_FAIL))
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}

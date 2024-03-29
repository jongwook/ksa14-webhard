package org.ksa14.webhard.ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;

/**
 * Maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel implements MsgListener {
	public static final long serialVersionUID = 0L;
	
	private static WebhardPanel theInstance;
	
	public static final int PANE_EXPLORE = 0;
	public static final int PANE_SEARCH = 1;
	public static final int PANE_TRANSFER = 2;

	public static final int TRANSFER_DOWNLOAD = 0;
	public static final int TRANSFER_UPLOAD = 1;
	
	private JLabel labelStatusBar;
	private ToolsBar barTools;
	private JPanel panelFile;
	private JSplitPane paneExplore;
	private JScrollPane paneSearch;
	private JTabbedPane paneTransfer;
	private ExploreDirectoryTree dirExplore;
	private ExploreFileList fileExplore;
	private SearchFileList fileSearch;
	private DownloadList fileDownload;
	private UploadList fileUpload;
	
	public WebhardPanel() {
		// Set layout and color
		setLayout(new BorderLayout());
		setBackground(Color.lightGray);

		// Initialize the status bar
		labelStatusBar = new JLabel("KSA14 Webhard");
		labelStatusBar.setBorder(BorderFactory.createEtchedBorder());
		add(labelStatusBar, BorderLayout.SOUTH);

		// Initialize the tool bar
		barTools = ToolsBar.getInstance();
		add(barTools, BorderLayout.NORTH);

		// Initialize the directory tree and explore file list 
		dirExplore = ExploreDirectoryTree.getInstance();
		JScrollPane paneDir = new JScrollPane(dirExplore);
		paneDir.setPreferredSize(new Dimension(250, 600));
		
		fileExplore = ExploreFileList.getInstance();
		JScrollPane paneEFile = new JScrollPane(fileExplore);
		paneEFile.getViewport().setBackground(Color.white);
		
		paneExplore = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paneDir, paneEFile);

		// Initialize the search file list
		fileSearch = SearchFileList.getInstance();
		paneSearch = new JScrollPane(fileSearch);
		paneSearch.getViewport().setBackground(Color.white);
		
		// Integrate file panels
		panelFile = new JPanel();
		panelFile.setLayout(new CardLayout());
		
		fileDownload = DownloadList.getInstance();
		JScrollPane paneDFile = new JScrollPane(fileDownload);
		paneDFile.getViewport().setBackground(Color.white);

		fileUpload = UploadList.getInstance();
		JScrollPane paneUFile = new JScrollPane(fileUpload);
		paneUFile.getViewport().setBackground(Color.white);
		
		paneTransfer = new JTabbedPane();
		paneTransfer.addTab("다운로드", paneDFile);
		paneTransfer.addTab("업로드", paneUFile);

		panelFile.add(paneExplore, "explore");
		panelFile.add(paneSearch, "search");
		panelFile.add(paneTransfer, "transfer");

		add(panelFile, BorderLayout.CENTER);

		MsgBroadcaster.addListener(this);
	}

	public static WebhardPanel getInstance() {
		return (theInstance == null) ? theInstance = new WebhardPanel() : theInstance;
	}
	
	private int getPaneIndex() {
		Component[] comps = panelFile.getComponents();
		int i;
		for (i=0; i<comps.length; i++) {
			if (comps[i].isVisible())
				return i;
		}
		return -1;
	}
	
	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.STATUS_INFO)
					labelStatusBar.setText(arg.toString());
				
				if (type == MsgListener.STATUS_MESSAGE)
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
				
				if (type == MsgListener.CONNECT_SUCCESS)
					dirExplore.initTree();
				
				if (type == MsgListener.CONNECT_FAIL) {
					dirExplore.setEnabled(false);
					fileExplore.setEnabled(false);
					fileSearch.setEnabled(false);
				}
				
				if (type == MsgListener.PANEL_EXPLORE)
					((CardLayout)panelFile.getLayout()).show(panelFile, "explore");
				
				if (type == MsgListener.PANEL_SEARCH)
					((CardLayout)panelFile.getLayout()).show(panelFile, "search");

				if (type == MsgListener.PANEL_TRANSFER)
					((CardLayout)panelFile.getLayout()).show(panelFile, "transfer");

				if (type == MsgListener.PANEL_DOWNLOAD) {
					((CardLayout)panelFile.getLayout()).show(panelFile, "transfer");
					paneTransfer.setSelectedIndex(TRANSFER_DOWNLOAD);
				}

				if (type == MsgListener.PANEL_UPLOAD) {
					((CardLayout)panelFile.getLayout()).show(panelFile, "transfer");
					paneTransfer.setSelectedIndex(TRANSFER_UPLOAD);
				}
				
				
				if (type == MsgListener.DOWNLOAD_CLICK) {
					int ipane = getPaneIndex();
					int[] srow = {};
					
					if (ipane == PANE_EXPLORE)
						srow = fileExplore.getSelectedRows();
					else if (ipane == PANE_SEARCH)
						srow = fileSearch.getSelectedRows();
					
					if (srow.length > 0) {
						JFileChooser savefile = new JFileChooser();
						savefile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						savefile.setAcceptAllFileFilterUsed(false);
						savefile.setDialogTitle("저장 위치");
						if (savefile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							final Vector<SftpTransferData> filelist = new Vector<SftpTransferData>();
							String pathsrc = dirExplore.getPath();
							String pathdest = savefile.getSelectedFile().toString();
							if (pathsrc.equals("/"))
								pathsrc = "";
							if (pathdest.charAt(pathdest.length() - 1) == File.separatorChar)
								pathdest = pathdest.substring(0, pathdest.length() - 1);
							
							for (int row : srow) {
								String filename = (String)fileExplore.getValueAt(row, FileList.COLUMN_FILENAME);
								boolean isdir = fileExplore.getValueAt(row, FileList.COLUMN_EXT).equals(".");
								filelist.add(new SftpTransferData(pathsrc, pathdest, filename, isdir, SftpTransfer.TRANSFER_DOWN));
							}
							
							new Thread() {
								public void run() {
									SftpTransfer.download(filelist);
								}
							}.start();
						}
					}
				}
				
				if (type == MsgListener.UPLOAD_CLICK) {
					if (dirExplore.getPath().equals("/")) {
						MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "최상위 폴더에는 업로드 할 수 없습니다.");
					} else {
						JFileChooser openfile = new JFileChooser();
						openfile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						openfile.setMultiSelectionEnabled(true);
						openfile.setDialogTitle("업로드할 파일/폴더");
						if (openfile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							final Vector<SftpTransferData> filelist = new Vector<SftpTransferData>();
							File[] filesup = openfile.getSelectedFiles();
							String pathsrc = filesup[0].getParent();
							String pathdest = dirExplore.getPath();
							if (pathsrc.charAt(pathsrc.length() - 1) == File.separatorChar)
								pathsrc = pathsrc.substring(0, pathsrc.length() - 1);
							
							for (File upf : filesup) {
								String filename = upf.getName();
								boolean isdir = upf.isDirectory();
								filelist.add(new SftpTransferData(pathsrc, pathdest, filename, isdir, SftpTransfer.TRANSFER_UP));
							}
							
							new Thread() {
								public void run() {
									SftpTransfer.upload(filelist);
								}
							}.start();
						}
					}
				}
			}
		});
	}
}

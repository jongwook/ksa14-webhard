package org.ksa14.webhard.ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

/**
 * Maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel implements MsgListener {
	public static final long serialVersionUID = 0L;
	
	public static final int FILEPANE_EXPLORE = 0;
	public static final int FILEPANE_SEARCH = 1;
	public static final int FILEPANE_TRANSFER = 2;

	private JLabel labelStatusBar;
	private ToolsBar barTools;
	private JPanel panelFile;
	private JSplitPane paneExplore;
	private JScrollPane paneSearch;
	private JTabbedPane paneTransfer;
	private ExploreDirectoryTree dirExplore;
	private ExploreFileList fileExplore;
	private SearchFileList fileSearch;
	/*
	private DownloadList fileDownload;
	private UploadList fileUpload;
	*/
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

		panelFile.add(paneExplore, "explore");
		panelFile.add(paneSearch, "search");
		//FilePanel.add(paneTransfer, "transfer");

		add(panelFile, BorderLayout.CENTER);

		MsgBroadcaster.addListener(this);
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
				
				if ((type == MsgListener.PANEL_EXPLORE))
					((CardLayout)panelFile.getLayout()).show(panelFile, "explore");
				
				if ((type == MsgListener.PANEL_SEARCH))
					((CardLayout)panelFile.getLayout()).show(panelFile, "search");
				
			}
		});
	}
}

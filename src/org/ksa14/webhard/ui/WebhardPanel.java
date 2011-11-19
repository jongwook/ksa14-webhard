package org.ksa14.webhard.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

/**
 * WebhardPanel maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel implements MsgListener {
	public static final long serialVersionUID = 0L;

	protected StatusBarLabel Status;
	protected JToolBar ToolsBar;
	protected JPanel FilePanel;
	protected DirectoryTree ExploreDir;
	protected ExploreFileList ExploreFile;
	protected FileList SearchFile;
	protected JSplitPane ExplorePane;
	protected JScrollPane SearchPane;

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
		
		// Initialize file panel
		FilePanel = new JPanel();
		FilePanel.setLayout(new CardLayout());
		
		FilePanel.add(ExplorePane, "explore");
		FilePanel.add(SearchPane, "search");

		add(FilePanel, BorderLayout.CENTER);
		
		// Add message listener to broadcaster
		MsgBroadcaster.AddListener(this);
		
		ExploreDir.UpdateTree();
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
				
				if ((type == MsgListener.DIRTREE_FAIL) || (type == MsgListener.FILELIST_FAIL) || (type == MsgListener.SEARCH_FAIL))
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}

package org.ksa14.webhard.ui;

import java.awt.BorderLayout;
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

	protected JToolBar toolBar;
	protected DirectoryTree dirTree;
	protected FileList files;
	protected StatusBarLabel statusBar;

	/**
	 * Initializes the GUI components of the main webhard window
	 */
	public WebhardPanel() {
		setBackground(Color.lightGray);
		
		// Set layout
		setLayout(new BorderLayout());

		// Initialize the tool bar
		toolBar = new WebhardToolBar();
		add(this.toolBar, BorderLayout.PAGE_START);

		// Initialize the directory tree
		dirTree = DirectoryTree.GetInstance();
		JScrollPane SPTree = new JScrollPane(this.dirTree);
		SPTree.setPreferredSize(new Dimension(200, 600));

		// Initialize the file list 
		files = FileList.GetInstance();
		JScrollPane SPList = new JScrollPane(this.files);
		SPList.getViewport().setBackground(Color.white);
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, SPTree, SPList), BorderLayout.CENTER);
		
		// Initialize the status bar
		statusBar = new StatusBarLabel("준비 중");
		add(this.statusBar, BorderLayout.PAGE_END);
		
		MsgBroadcaster.AddListener(this);
		MsgBroadcaster.AddListener(statusBar);
	}

	@Override
	public void ReceiveMsg(final int type, final Object arg) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if ((type == MsgListener.DIRTREE_FAIL) || (type == MsgListener.FILELIST_FAIL))
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}

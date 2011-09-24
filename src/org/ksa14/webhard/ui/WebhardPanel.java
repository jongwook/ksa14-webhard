package org.ksa14.webhard.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.ksa14.webhard.sftp.*;

/**
 * WebhardPanel maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel implements SftpListener {
	public static final long serialVersionUID = 0L;

	protected JToolBar toolBar;
	protected DirectoryTree tree;
	protected FileList files;
	protected JLabel statusBar;

	/**
	 * Initializes the GUI components of the main webhard window
	 */
	public WebhardPanel() {
		this.setBackground(Color.lightGray);
		// Set layout
		this.setLayout(new BorderLayout());

		// Initialize the tool bar
		this.toolBar = new WebhardToolBar();
		this.add(this.toolBar, BorderLayout.PAGE_START);

		// Initialize the directory tree
		this.tree = DirectoryTree.GetInstance();
		JScrollPane SPTree = new JScrollPane(this.tree);
		SPTree.setPreferredSize(new Dimension(200, 600));

		// Initialize the file list 
		this.files = FileList.GetInstance();
		JScrollPane SPList = new JScrollPane(this.files);
		SPList.getViewport().setBackground(Color.white);
		
		this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, SPTree, SPList), BorderLayout.CENTER);
		
		// Initialize the status bar
		this.statusBar = new JLabel("준비 중");
		this.statusBar.setBorder(BorderFactory.createEtchedBorder());
		this.statusBar.setBackground(Color.lightGray);
		this.add(this.statusBar, BorderLayout.PAGE_END);
		
		SftpAdapter.AddListener(this);
	}
	
	public void UpdateStatus(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(type == SftpListener.INFO && statusBar != null) {
					statusBar.setText(arg.toString());
				}
				
				if(type == SftpListener.DIRLIST_DONE) {
					Vector<?> dirList = (Vector<?>)arg;
					DirectoryTree.GetInstance().UpdateTreeDone(dirList);
				}
				
				if(type == SftpListener.FILELIST_DONE) {
					Vector<?> list = (Vector<?>)arg;
					FileList.GetInstance().UpdateListDone(list);
				}
				
				if(type == SftpListener.FAILED){
					JOptionPane.showMessageDialog(null, arg.toString(), "KSA14 Webhard", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
}

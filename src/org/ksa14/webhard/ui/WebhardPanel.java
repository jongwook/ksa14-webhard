package org.ksa14.webhard.ui;

import java.awt.*;
import javax.swing.*;

/**
 * WebhardPanel maintains all components in the client area of the main webhard window
 * 
 * @author Jongwook
 */
public class WebhardPanel extends JPanel {
	public static final long serialVersionUID = 0L;

	protected JToolBar toolBar;
	protected DirectoryTree tree;
	protected FileList files;

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
	}
}

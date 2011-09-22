package org.ksa14.webhard.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * DirectoryTree represents the directory tree component that goes left of the webahrd window. 
 * It shows the hierarchy of directories in the remote webhard server, 
 * and should initiate appropriate sftp requests as it gets user inputs.
 * 
 * @author Jongwook
 */
public class DirectoryTree extends JTree {
	public static final long serialVersionUID = 0L;
	
	DefaultMutableTreeNode top;
	
	/**
	 * Initializes the directory tree view 
	 */
	public DirectoryTree() {
		top = new DefaultMutableTreeNode("KSA14 Webhard");
		
		this.setPreferredSize(new Dimension(200,600));
	}
}

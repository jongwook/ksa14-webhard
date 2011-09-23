package org.ksa14.webhard.ui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import org.ksa14.webhard.sftp.*;

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
	public DirectoryTree(DefaultMutableTreeNode tnode) {
		super (tnode);
		top = tnode;
		
		UpdateTree(top, "/");
		
		this.expandRow(0);
		this.setScrollsOnExpand(true);
	}
	
	private void UpdateTree(DefaultMutableTreeNode parent, String path) {
		Vector<String> dirlist = SftpUtil.GetDirectoryList(path);
		for (int i=0; i<dirlist.size(); i++) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(dirlist.elementAt(i));
			UpdateTree(child, path + "/" + dirlist.elementAt(i));
			parent.add(child);
		}
	}
}

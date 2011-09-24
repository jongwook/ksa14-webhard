package org.ksa14.webhard.ui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.ksa14.webhard.sftp.*;

/**
 * DirectoryTree represents the directory tree component that goes left of the webahrd window. 
 * It shows the hierarchy of directories in the remote webhard server, 
 * and should initiate appropriate sftp requests as it gets user inputs.
 * 
 * @author Jongwook
 */
public class DirectoryTree extends JTree implements TreeSelectionListener {
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
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.addTreeSelectionListener(this);
	}
	
	private void UpdateTree(DefaultMutableTreeNode parent, String path) {
		Vector<String> dirlist = SftpUtil.GetDirectoryList(path);
		for (int i=0; i<dirlist.size(); i++) {
			String dir = dirlist.elementAt(i);
			
			// skip hidden files
			if(dir.charAt(0) == '.') continue;
			
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(dirlist.elementAt(i));
			
			// save the directory path to the object
			//child.setUserObject(path + "/" + dirlist.elementAt(i));
			
			// create dummy child
			child.add(new DefaultMutableTreeNode("..."));
			
			parent.add(child);
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if(node == null) return;
		
		Object paths[] = node.getUserObjectPath();
		StringBuffer path = new StringBuffer();
		for(int depth = 1; depth < paths.length; ++depth) {
			path.append("/" + paths[depth]);
		}
		
		if(!node.isLeaf() && node.getChildAt(0).toString().equals("...")) {
			node.remove(0);
			UpdateTree(node,path.toString());
		}
	}
}

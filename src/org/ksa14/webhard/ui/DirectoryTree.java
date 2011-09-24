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
public class DirectoryTree extends JTree implements TreeSelectionListener, TreeWillExpandListener {
	public static final long serialVersionUID = 0L;

	private static DirectoryTree theInstance;
	DefaultMutableTreeNode top;

	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {
		public static final long serialVersionUID = 0L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (leaf && tree.getPathForRow(row).getLastPathComponent() instanceof DefaultMutableTreeNode) { 
				setLeafIcon(getClosedIcon());
			}    
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,	row, hasFocus);
		}
	}

	/**
	 * Initializes the directory tree view 
	 */
	private DirectoryTree(DefaultMutableTreeNode tnode) {
		super (tnode);
		top = tnode;

		UpdateTree(top, "/");

		this.expandRow(0);
		this.setScrollsOnExpand(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.addTreeSelectionListener(this);
		this.addTreeWillExpandListener(this);
		this.setCellRenderer(new MyTreeCellRenderer());
	}
	
	public static DirectoryTree GetInstance() {
		if(theInstance == null) {
			theInstance = new DirectoryTree(new DefaultMutableTreeNode("KSA14 Webhard"));
		}
		return theInstance;		
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
		UpdateNode(e.getPath().getPath(), node);
	}

	public void treeWillExpand(TreeExpansionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
		UpdateNode(e.getPath().getPath(), node);
		this.setSelectionPath(e.getPath());
	}
	
	public void treeWillCollapse(TreeExpansionEvent e)  {}
	
	public void UpdateNode(Object paths[], DefaultMutableTreeNode node) {
		if(node == null) return;		
		
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

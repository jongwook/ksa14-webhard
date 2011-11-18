package org.ksa14.webhard.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpList;

/**
 * DirectoryTree represents the directory tree component that goes left of the webahrd window. 
 * It shows the hierarchy of directories in the remote webhard server, 
 * and should initiate appropriate sftp requests as it gets user inputs.
 * 
 * @author Jongwook
 */
public class DirectoryTree extends JTree implements TreeSelectionListener, TreeWillExpandListener, MsgListener {
	public static final long serialVersionUID = 0L;

	private static DirectoryTree theInstance;
	DefaultMutableTreeNode top, lastNode;
	TreePath lastPath;

	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {
		public static final long serialVersionUID = 0L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (leaf && tree.getPathForRow(row).getLastPathComponent() instanceof DefaultMutableTreeNode)
				setLeafIcon(getClosedIcon());
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}

	/**
	 * Initializes the directory tree view 
	 */
	private DirectoryTree(DefaultMutableTreeNode tnode) {
		super (tnode);
		
		top = tnode;
		top.add(new DefaultMutableTreeNode("..."));
		lastNode = top;
		lastPath = new TreePath(top);

		setScrollsOnExpand(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		addTreeWillExpandListener(this);
		setRowHeight(20);
		setCellRenderer(new MyTreeCellRenderer());
		
		MsgBroadcaster.AddListener(this);
		
		Object paths[] = {"/", ""};
		UpdateTree(paths, top);
	}

	public static DirectoryTree GetInstance() {
		if (theInstance == null) 
			theInstance = new DirectoryTree(new DefaultMutableTreeNode("KSA14 Webhard"));

		return theInstance;	
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if (node == lastNode)
			return;
		
		lastPath = e.getPath();
		lastNode = node;
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree(e.getPath().getPath(), node);
	}

	public void treeWillExpand(TreeExpansionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
		if (node == top || node == lastNode)
			return;
		
		lastPath = e.getPath();
		lastNode = node;
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree(e.getPath().getPath(), node);
		setSelectionPath(e.getPath());
	}

	public void treeWillCollapse(TreeExpansionEvent e) {}
	
	public void UpdateTree(final Object paths[], final DefaultMutableTreeNode node) {
		if (node == null)
			return;
		
		setEnabled(false);
		FileList.GetInstance().setEnabled(false);
		
		new Thread() { 
			public void run() {
				StringBuffer path = new StringBuffer();
				for (int depth = 1; depth < paths.length; depth++)
					path.append("/" + paths[depth]);

				if (!node.isLeaf() && node.getChildAt(0).toString().equals("...")) {
					node.remove(0);
					SftpList.GetDirectoryList(path.toString());
				} else {
					MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
					UpdateTreeDone(new Vector<Object>());
				}

				if (path.length() == 0)
					FileList.GetInstance().UpdateList("/");
				else
					FileList.GetInstance().UpdateList(path.toString());
			}
		}.start();
	}

	public void UpdateTreeDone(Vector<?> dirlist) {
		Iterator<?> dirIter = dirlist.iterator();
		while (dirIter.hasNext()) {
			Object curItem = dirIter.next();

			// Skip hidden files
			if (curItem.toString().charAt(0) == '.')
				continue;

			// Create dummy child
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(curItem);
			child.add(new DefaultMutableTreeNode("..."));
			lastNode.add(child);
		}
		
		collapsePath(lastPath);
		expandPath(lastPath);
		
		setEnabled(true);
	}
	
	public void ChangeDirectory(String directory) {
		DefaultMutableTreeNode childNode = null;
		for (int i=0; i<lastNode.getChildCount(); i++) {
			if(lastNode.getChildAt(i).toString().equals(directory)) {
				childNode = (DefaultMutableTreeNode)lastNode.getChildAt(i);
				break;
			}
		}
		if (childNode == null)
			return;
		
		lastNode = childNode;
		lastPath = lastPath.pathByAddingChild(lastNode);
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree(lastPath.getPath(), lastNode);
		setSelectionPath(lastPath);
	}

	@Override
	public void ReceiveMsg(final int type, final Object arg) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.DIRTREE_DONE)
					GetInstance().UpdateTreeDone((Vector<?>)arg);
				
				if (type == MsgListener.DIRTREE_FAIL)
					GetInstance().UpdateTreeDone(new Vector<Object>());
			}
		});
	}
}

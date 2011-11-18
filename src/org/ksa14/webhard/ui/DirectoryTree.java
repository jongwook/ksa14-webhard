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
	
	private class DirectoryTreePath extends TreePath {
		private static final long serialVersionUID = 0L;
		
		public DirectoryTreePath(DefaultMutableTreeNode node) {
			super(node);
		}
		
		public DirectoryTreePath(Object[] path) {
			super(path);
		}
		
		public DirectoryTreePath(TreePath path) {
			super(path.getPath());
		}

		public String toString() {
			Object[] paths = this.getPath();
			StringBuffer path = new StringBuffer();
			for (int depth = 1; depth < paths.length; depth++)
				path.append("/" + paths[depth]);
			if (path.length() == 0)
				return "/";
			return path.toString();
		}
	}

	private static DirectoryTree TheInstance;
	private DefaultMutableTreeNode TopNode, LastNode;
	private DirectoryTreePath LastPath;

	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {
		public static final long serialVersionUID = 0L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (leaf && tree.getPathForRow(row).getLastPathComponent() instanceof DefaultMutableTreeNode)
				setLeafIcon(getClosedIcon());
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}
	
	private DirectoryTree(DefaultMutableTreeNode tnode) {
		super (tnode);
		
		TopNode = tnode;
		TopNode.add(new DefaultMutableTreeNode("..."));
		
		LastPath = new DirectoryTreePath(TopNode);
		LastNode = TopNode;

		setScrollsOnExpand(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		addTreeWillExpandListener(this);
		setRowHeight(20);
		setCellRenderer(new MyTreeCellRenderer());
		
		MsgBroadcaster.AddListener(this);
	}

	public static DirectoryTree GetInstance() {
		if (TheInstance == null) 
			TheInstance = new DirectoryTree(new DefaultMutableTreeNode("KSA14 Webhard"));

		return TheInstance;	
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if (node == LastNode)
			return;
		
		LastPath = new DirectoryTreePath(e.getPath());
		LastNode = node;
		
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree();
	}

	public void treeWillExpand(TreeExpansionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
		if (node == TopNode || node == LastNode)
			return;
		
		LastPath = new DirectoryTreePath(e.getPath());
		LastNode = node;
		
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree();
		setSelectionPath(e.getPath());
	}

	public void treeWillCollapse(TreeExpansionEvent e) {}
	
	public void UpdateTree() {
		if (LastNode == null)
			return;
		
		ExploreFileList.GetInstance().setEnabled(false);
		
		new Thread() {
			public void run() {
				if (!LastNode.isLeaf() && LastNode.getChildAt(0).toString().equals("...")) {
					LastNode.remove(0);
					SftpList.GetDirectoryList(LastPath.toString());
				} else {
					MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
				}
				
				ExploreFileList.GetInstance().UpdateList(LastPath.toString());
			}
		}.start();
	}
	
	public void UpdateTree(String path) {
	}

	public void UpdateTreeDone(Vector<?> dirlist) {
		Iterator<?> diri = dirlist.iterator();
		while (diri.hasNext()) {
			Object item = diri.next();

			// Create dummy child
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(item);
			child.add(new DefaultMutableTreeNode("..."));
			LastNode.add(child);
		}
		
		collapsePath(LastPath);
		expandPath(LastPath);
	}
	
	public void ChangeDirectory(String directory) {
		DefaultMutableTreeNode childNode = null;
		for (int i=0; i<LastNode.getChildCount(); i++) {
			if(LastNode.getChildAt(i).toString().equals(directory)) {
				childNode = (DefaultMutableTreeNode)LastNode.getChildAt(i);
				break;
			}
		}
		if (childNode == null)
			return;
		
		LastNode = childNode;
		LastPath = new DirectoryTreePath(LastPath.pathByAddingChild(LastNode));
		
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		UpdateTree();
		
		setSelectionPath(LastPath);
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

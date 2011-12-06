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

public class ExploreDirectoryTree  extends JTree implements TreeSelectionListener, TreeWillExpandListener, MsgListener {
	public static final long serialVersionUID = 0L;
	
	public class DirectoryTreePath extends TreePath {
		private static final long serialVersionUID = 0L;
		
		public DirectoryTreePath(DefaultMutableTreeNode node) {
			super(node);
		}
		
		public DirectoryTreePath(TreePath path) {
			super(path.getPath());
		}

		public String toString() {
			Object[] paths = getPath();
			StringBuffer path = new StringBuffer();
			for (int depth = 1; depth < paths.length; depth++)
				path.append("/" + paths[depth]);
			if (path.length() == 0)
				return "/";
			return path.toString();
		}
	}
	
	private static ExploreDirectoryTree theInstance;

	private DefaultMutableTreeNode nodeTop, nodeLast;
	private DirectoryTreePath pathLast;

	private ExploreDirectoryTree(DefaultMutableTreeNode tnode) {
		super(tnode);
		
		nodeTop = tnode;

		setScrollsOnExpand(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		addTreeWillExpandListener(this);
		setCellRenderer(new DefaultTreeCellRenderer(){
			public static final long serialVersionUID = 0L;

			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				if (leaf && (tree.getPathForRow(row).getLastPathComponent() instanceof DefaultMutableTreeNode))
					setLeafIcon(getClosedIcon());
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			}
		});
		setRowHeight(20);
		
		MsgBroadcaster.addListener(this);
	}
	
	public static ExploreDirectoryTree getInstance() {
		return (theInstance == null) ? (theInstance = new ExploreDirectoryTree(new DefaultMutableTreeNode("KSA14 Webhard"))) : theInstance;	
	}
	
	public void initTree() {
		nodeTop.removeAllChildren();
		nodeTop.add(new DefaultMutableTreeNode("..."));
		
		pathLast = new DirectoryTreePath(nodeTop);
		nodeLast = nodeTop;
		
		updateTree();
	}
	
	public void updateTree() {
		if ((nodeLast == null) || (pathLast == null))
			return;
		
		setEnabled(false);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		new Thread() {
			public void run() {
				if (!nodeLast.isLeaf() && nodeLast.getChildAt(0).toString().equals("...")) {
					nodeLast.remove(0);
					SftpList.getDirectoryList(pathLast.toString());
				} else {
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
					MsgBroadcaster.broadcastMsg(MsgListener.DIRTREE_DONE, new Vector<String>());
				}
			}
		}.start();
	}
	
	public void updateTreeDone(Vector<?> dirlist) {
		if (dirlist == null)
			return;
		
		Iterator<?> diriter = dirlist.iterator();
		while (diriter.hasNext()) {
			String dirname = (String)diriter.next();
			
			// Create dummy child
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(dirname);
			node.add(new DefaultMutableTreeNode("..."));
			nodeLast.add(node);
		}

		collapsePath(pathLast);
		expandPath(pathLast);
		
		setEnabled(true);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void changePathChild(String child) {
		DefaultMutableTreeNode nodeChild = null;
		for (int i=0; i<nodeLast.getChildCount(); i++) {
			if (nodeLast.getChildAt(i).toString().equals(child)) {
				nodeChild = (DefaultMutableTreeNode)nodeLast.getChildAt(i);
				break;
			}
		}
		if (nodeChild == null)
			return;
		
		pathLast = new DirectoryTreePath(pathLast.pathByAddingChild(nodeChild));
		nodeLast = nodeChild;
		
		updateTree();
		setSelectionPath(pathLast);
	}

	public void changePath(String path) {
		pathLast = new DirectoryTreePath(nodeTop);
		nodeLast = nodeTop;
		
		String[] dirpath = path.split("/");
		for (int i=1; i<dirpath.length; i++) {
			if (!nodeLast.isLeaf()) {
				if (nodeLast.getChildAt(0).toString().equals("...")) {
					nodeLast.remove(0);
					updateTreeDone(SftpList.getDirectoryListSeq(pathLast.toString()));
				}
				
			}
			
			DefaultMutableTreeNode nodeChild = null;
			for (int j=0; j<nodeLast.getChildCount(); j++) {
				if (nodeLast.getChildAt(j).toString().equals(dirpath[i])) {
					nodeChild = (DefaultMutableTreeNode)nodeLast.getChildAt(j);
					break;
				}
			}
			if (nodeChild == null)
				break;

			pathLast = new DirectoryTreePath(pathLast.pathByAddingChild(nodeChild));
			nodeLast = nodeChild;
		}
		
		updateTree();
		setSelectionPath(pathLast);
	}

	public String getPath() {
		return pathLast.toString();
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if (node == nodeLast)
			return;
		
		pathLast = new DirectoryTreePath(e.getPath());
		nodeLast = node;
		
		updateTree();
	}

	public void treeWillExpand(TreeExpansionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
		if ((node == nodeTop) || (node == nodeLast))
			return;
		
		pathLast = new DirectoryTreePath(e.getPath());
		nodeLast = node;
		
		updateTree();
		setSelectionPath(pathLast);
	}

	public void treeWillCollapse(TreeExpansionEvent e) {}
	
	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.DIRTREE_DONE) {
					getInstance().updateTreeDone((Vector<?>)arg);
					ExploreFileList.getInstance().updateList(getPath());
				}
				
				if (type == MsgListener.DIRTREE_FAIL)
					getInstance().updateTreeDone(new Vector<String>());
			}
		});
	}
}

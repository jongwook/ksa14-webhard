package org.ksa14.webhard.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class DirectoryTree extends JTree {
	public static final long serialVersionUID = 0;
	
	DefaultMutableTreeNode top;
	
	public DirectoryTree() {
		top = new DefaultMutableTreeNode("KSA14 Webhard");
		
		this.setPreferredSize(new Dimension(200,600));
	}
}

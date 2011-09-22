package org.ksa14.webhard.ui;

import java.awt.*;
import javax.swing.*;

public class WebhardView extends JFrame{
	public static final long serialVersionUID=0;
	
	protected JToolBar toolBar;
	protected DirectoryTree tree;
	
	public WebhardView() {
		// Create the main window with the title
		super("KSA14 Webhard Client");
		
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();
		
		// Make the process terminate when the window is closed 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the background color
		this.getContentPane().setBackground(Color.lightGray);
		
		// Set layout
		this.setLayout(new BorderLayout());
		this.setSize(new Dimension(800,600));
		
		// Initialize the tool bar
		this.toolBar = new JToolBar("KSA14 Webhard Tool Bar");
		this.add(this.toolBar, BorderLayout.PAGE_START);
		
		// Initialize the directory tree
		this.tree = new DirectoryTree();
		this.add(this.tree, BorderLayout.LINE_START);
		
		// temp
		this.add(new Button("test"), BorderLayout.CENTER);
		
		// Finally, show the window up
		this.setVisible(true);
	}
	
	public static void main(String ... args) {
		new WebhardView();
	}
}

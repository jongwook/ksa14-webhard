package org.ksa14.webhard.ui;

import java.awt.*;

import javax.swing.*;

/**
 * WebhardFrame is a JFrame subclass that represents the main window 
 * 
 * @author Jongwook
 */
public class WebhardFrame extends JFrame{
	public static final long serialVersionUID=0L;
	public static final int wWidth = 800;
	public static final int wHeight = 600;
	public static WebhardFrame theInstance;
	/**
	 * Initialize the main webhard window. 
	 * GUI components are initialized by WebhardPanel class.
	 */
	private WebhardFrame() {
		// Create the main window with the title
		super("KSA14 Webhard Client");
		
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();
		
		// Make the process terminate when the window is closed 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the default window size and location
		int sW = (int)getToolkit().getScreenSize().getWidth();
		int sH = (int)getToolkit().getScreenSize().getHeight();
		int wW = Math.min(sW, wWidth);
		int wH = Math.min(sH, wHeight);
		this.setSize(wW, wH);
		this.setLocation((sW - wW) / 2, (sH - wH) / 2);
		
		// Set the background color
		this.getContentPane().setBackground(Color.lightGray);
		
		// Add the main panel
		this.add(new WebhardPanel());
		
		// Show the wait cursor until the file list loads up
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Finally, show the window up
		this.setVisible(true);
	}
	
	public static WebhardFrame GetInstance() {
		return (theInstance == null) ? theInstance=new WebhardFrame() : theInstance;
	}
	
	public static void Open() {
		GetInstance();
	}
}

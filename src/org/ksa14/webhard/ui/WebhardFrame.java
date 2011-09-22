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
	
	/**
	 * Initialize the main webhard window. 
	 * GUI components are initialized by WebhardPanel class.
	 */
	public WebhardFrame() {
		// Create the main window with the title
		super("KSA14 Webhard Client");
		
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();
		
		// Make the process terminate when the window is closed 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the default window size
		this.setSize(new Dimension(800,600));
		
		// Set the background color
		this.getContentPane().setBackground(Color.lightGray);
		
		// Add the main panel
		this.add(new WebhardPanel());
		
		// Finally, show the window up
		this.setVisible(true);
	}
}

package org.ksa14.webhard.ui;

import java.awt.*;
import java.net.URL;

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
		
		// Load Icon
		URL urlIcon = getClass().getResource("/res/icon48.png");
		Image imgIcon = Toolkit.getDefaultToolkit().getImage(urlIcon);
		setIconImage(imgIcon);
		
		// Make the process terminate when the window is closed 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the default window size and location
		int sW = (int)getToolkit().getScreenSize().getWidth();
		int sH = (int)getToolkit().getScreenSize().getHeight();
		int wW = Math.min(sW, wWidth);
		int wH = Math.min(sH, wHeight);
		setSize(wW, wH);
		setLocation((sW - wW) / 2, (sH - wH) / 2);
		
		// Set the background color
		getContentPane().setBackground(Color.lightGray);
		
		// Add the main panel
		add(new WebhardPanel());
		
		// Show the wait cursor until the file list loads up
		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Finally, show the window up
		setVisible(true);
	}
	
	public static WebhardFrame GetInstance() {
		return (theInstance == null) ? theInstance = new WebhardFrame() : theInstance;
	}
	
	public static void Open() {
		GetInstance();
	}
	
	public static void Exit() {
		if (JOptionPane.showOptionDialog(null, "KSA14 Webhard 를 종료합니다", "KSA14 Webhard Client", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0)
			System.exit(0);
	}
}

package org.ksa14.webhard.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Represents the main webhard window 
 * 
 * @author Jongwook, ThomasJun
 */
public class WebhardFrame extends JFrame {
	public static final long serialVersionUID = 0L;

	public static WebhardFrame TheInstance;
	
	private int Width = 1024;
	private int Height = 600;

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
		
		// Set the default window size and location
		int sW = (int)getToolkit().getScreenSize().getWidth();
		int sH = (int)getToolkit().getScreenSize().getHeight();
		int wW = Math.min(sW, Width);
		int wH = Math.min(sH, Height);
		setSize(wW, wH);
		setLocation((sW - wW) / 2, (sH - wH) / 2);

		// Set the background color
		getContentPane().setBackground(Color.lightGray);

		// Add the webhard main panel
		add(new WebhardPanel());
		
		// Add listener for closing the window 
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}
	
	public static WebhardFrame getInstance() {
		return (TheInstance == null) ? TheInstance = new WebhardFrame() : TheInstance;
	}
	
	public static void open() {
		getInstance().setVisible(true);
		authenticate();
	}
	
	public static void authenticate() {
		WebhardAuth.open();
		if (WebhardAuth.authed) {}			
	}

	public static void exit() {
		if (JOptionPane.showOptionDialog(null, "KSA14 Webhard 를 종료합니다", "KSA14 Webhard Client", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0)
			System.exit(0);
	}
}

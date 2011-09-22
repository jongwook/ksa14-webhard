package org.ksa14.webhard.ui;

import javax.swing.UIManager;

public class SwingUtility {
	
	// Try to set system native look-and-feel
	public static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			System.err.println("Unable to set native LAF : " + e.getMessage());
		}
	}
	
	
}

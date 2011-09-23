package org.ksa14.webhard;

import org.ksa14.webhard.ui.*;
import org.ksa14.webhard.sftp.*;

/**
 * The main class
 * 
 * @author Jongwook
 */
public class Main {
	public static SftpUtil Sftp;
	
	/**
	 * The entry point of the program.
	 * 
	 * @param args the command line arguments. will not be used.
	 */
	public static void main(String ... args) {		
		//TODO : Setup sftp connection
		Sftp = new SftpUtil();
		
		// Check authentication
		if (!AuthDialog.Authenticate(Sftp))
			return;
		
		// Open the main window if the authentication was successful.
		new WebhardFrame();
	}
}

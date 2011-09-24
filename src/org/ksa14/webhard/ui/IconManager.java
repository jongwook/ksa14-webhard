package org.ksa14.webhard.ui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class IconManager {
	private IconManager() {}
	
	private static HashMap<String, Icon> icons = new HashMap<String, Icon>();
	private static Icon defaultIcon = Get("");
	private static FileSystemView view;
	
	public static Icon Get(String filename) {
		int index = filename.lastIndexOf('.');
		String extension = (index != -1) ? filename.substring(index) : "";
		if(icons.containsKey(extension)) {
			return icons.get(extension);
		} 
		try {
			File file = File.createTempFile(UUID.randomUUID().toString(), extension);
			if(view == null) {
				view = FileSystemView.getFileSystemView();
			}
			return view.getSystemIcon(file);
		} catch(IOException e) {
			System.err.println("Error searching the system icon for " + extension);
		}
		return defaultIcon;
	}
}

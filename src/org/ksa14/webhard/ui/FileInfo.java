package org.ksa14.webhard.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

public class FileInfo {
	private FileInfo() {}

	private static HashMap<String, Icon> icons = new HashMap<String, Icon>();	
	private static FileSystemView view;

	public static Icon GetIcon(String extension) {		
		if(!icons.containsKey(extension)) {
			try {
				File file = File.createTempFile(UUID.randomUUID().toString(), "." + extension);
				if(extension.equals(".")) {
					file.delete();
					file.mkdir();
				}
				if(view == null)
					view = FileSystemView.getFileSystemView();
				icons.put(extension, view.getSystemIcon(file));
				
				return icons.get(extension);
			} catch(IOException e) {
				System.err.println("Error searching the system icon for " + extension);
			}
		}

		return icons.get(extension);
	}

	public static String GetDescription(String extension) {
		if(extension.equals("."))
			return "폴더 ";
		return GetIcon(extension).toString();
	}
}

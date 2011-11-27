package org.ksa14.webhard.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * Contains utility features related to file.
 * 
 * @author Jongwook
 */
public class FileUtility {
	private static HashMap<String, Icon> icons = new HashMap<String, Icon>();	
	private static FileSystemView view;

	public static Icon getIcon(String ext) {		
		if(!icons.containsKey(ext)) {
			try {
				File file = File.createTempFile(UUID.randomUUID().toString(), "." + ext);
				if(ext.equals(".")) {
					file.delete();
					file.mkdir();
				}
				if(view == null)
					view = FileSystemView.getFileSystemView();
				icons.put(ext, view.getSystemIcon(file));
			} catch(IOException e) {
				System.err.println("Error searching the system icon for " + ext);
			}
		}

		return icons.get(ext);
	}

	public static String getDescription(String ext) {
		if(ext.equals("."))
			return "폴더 ";
		return getIcon(ext).toString();
	}
	
	public static String getFileSize(long size) {
		return getFileSize((float)size);
	}
	
	public static String getFileSize(float size) {
		String[] units = {"B", "KB", "MB", "GB"};
		
		for (int i=0; i<units.length; i++) {
			if (size < 1024.0) 
				return String.format("%.1f %s", size, units[i]);
			size /= 1024.0;
		}
		
		return String.format("%.1f TB", size);
	}
}

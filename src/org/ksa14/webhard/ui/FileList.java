package org.ksa14.webhard.ui;

import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

import org.ksa14.webhard.sftp.SftpUtil;
import com.jcraft.jsch.ChannelSftp.*;

/**
 * FileList represents the GUI component that shows lists of files in the directory selected in DirectoryTree component 
 * 
 * @author Jongwook
 */
public class FileList extends JTable {
	public static final long serialVersionUID = 0L;
	
	private static FileList theInstance;
	private static DateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
	
	// TODO consider moving this part to a separate resource file
	public static final String columns[] = {"이름", "크기", "종류", "날짜"};
	public static final Object rows[][] = {};

	/**
	 * The default table model 
	 */
	protected static DefaultTableModel model = new DefaultTableModel(FileList.rows, FileList.columns) {
		public static final long serialVersionUID = 0L;
		
        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
	
	
	/**
	 * Initializes the table and its columns
	 */
	private FileList() {
		super(FileList.model);
		
		// swing internal property to act like file chooser. 
		// might not be a good idea to use this private API
		this.putClientProperty("Table.isFileList", Boolean.TRUE);
		
		this.setCellSelectionEnabled(true);
		this.setIntercellSpacing(new Dimension());
		this.setShowGrid(false);
		this.getColumnModel().getColumn(0).setPreferredWidth(400);
	}
	
	public static FileList GetInstance() {
		if(theInstance == null) {
			theInstance = new FileList();
		}
		return theInstance;
	}
	
	public void UpdateList(String path) {		
		Vector<LsEntry> list = SftpUtil.GetFilesList(path);
		
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);
		
		for(LsEntry entry : list) {
			Object row[] = {
					entry.getFilename(),
					FileSize(entry.getAttrs().getSize()),
					"파일",
					dateFormat.format(new Date(entry.getAttrs().getMTime() * 1000L))
			};
			model.addRow(row);
		}
	}
	
	private String FileSize(long size) {
		double fSize = size;
		String units[] = {"B", "KB", "MB", "GB"};
		for(int i=0; i<4; ++i) {
			if(fSize < 1024.0) {
				return String.format("%.1f %s", fSize, units[i]);
			} 
			fSize /= 1024.0;
		}
		return String.format("%.1f %s", fSize, "TB");
	}
}

package org.ksa14.webhard.ui;

import java.awt.*;
import java.awt.event.*;
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
	
	public static final int SORT_FILENAME		= 0;
	public static final int SORT_SIZE			= 1;
	public static final int SORT_EXT			= 2;
	public static final int SORT_DATE			= 3;
	
	public static int SortMode = SORT_FILENAME;
	public static boolean Asc = true;

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

	public class MyTableCellRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {			
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			String text = value.toString();
			if (column == 0){
				label.setText(text);
				label.setIcon(IconManager.Get(text));
			} else if (column == 1) {
				label.setText(FileSize(text));
				label.setIcon(null);
			} else if (column == 2) {
				label.setText("파일");
				label.setIcon(null);	
			} else if (column == 3) {
				label.setText(dateFormat.format(new Date(Long.parseLong(text))));
				label.setIcon(null);			
			} else {
				label.setText(text);
				label.setIcon(null);
			}
			return label;
		}
	}
	
	public class ColumnHeaderListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTable table = FileList.GetInstance();
			TableColumnModel colModel = table.getColumnModel();

	        // The index of the column whose header was clicked
	        int vColIndex = colModel.getColumnIndexAtX(e.getX());
	        int mColIndex = table.convertColumnIndexToModel(vColIndex);
	        
	        // Return if not clicked on any column header
	        if (vColIndex == -1) {
	            return;
	        }
	        
	        // Determine if mouse was clicked between column heads
	        Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
	        if (vColIndex == 0) {
	            headerRect.width -= 3;    // Hard-coded constant
	        } else {
	            headerRect.grow(-3, 0);   // Hard-coded constant
	        }
	        if (!headerRect.contains(e.getX(), e.getY())) {
	            // Mouse was clicked between column heads
	            // vColIndex is the column head closest to the click

	            // vLeftColIndex is the column head to the left of the click
	            int vLeftColIndex = vColIndex;
	            if (e.getX() < headerRect.x) {
	                vLeftColIndex--;
	            }
	        }
	        
	        Sort(vColIndex);
		}
	}

	private class ModelComparator implements Comparator {
		private int mode = 0;
		private boolean asc = true;
		
		public ModelComparator(int m, boolean a) {
			this.mode = m;
			this.asc = a;
		}
		
		public int compare(Object arg0, Object arg1) {
			Vector v0 = (Vector)arg0;
			Vector v1 = (Vector)arg1;
			
			long s0, s1;
			switch (mode) {
			case SORT_SIZE:
				s0 = Long.parseLong((String)v0.elementAt(1));
				s1 = Long.parseLong((String)v1.elementAt(1));
				if (asc)
					return (s0 > s1) ? 1 : ((s0 < s1) ? -1 : 0);
				else
					return (s0 > s1) ? -1 : ((s0 < s1) ? 1 : 0);
			case SORT_EXT:
				if (asc)
					return ((String)v0.elementAt(2)).compareTo((String)v1.elementAt(2));
				else
					return ((String)v1.elementAt(2)).compareTo((String)v0.elementAt(2));
			case SORT_DATE:
				s0 = Long.parseLong((String)v0.elementAt(3));
				s1 = Long.parseLong((String)v1.elementAt(3));
				if (asc)
					return (s0 > s1) ? 1 : ((s0 < s1) ? -1 : 0);
				else
					return (s0 > s1) ? -1 : ((s0 < s1) ? 1 : 0);
			case SORT_FILENAME:
			default:
				if (asc)
					return ((String)v0.elementAt(0)).compareTo((String)v1.elementAt(0));
				else
					return ((String)v1.elementAt(0)).compareTo((String)v0.elementAt(0));
			}
		}
	}

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
		this.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		
		this.getTableHeader().addMouseListener(new ColumnHeaderListener());
	}

	public static FileList GetInstance() {
		if(theInstance == null) {
			theInstance = new FileList();
		}
		return theInstance;
	}
	
	public void UpdateList(String path) {
		Vector<LsEntry> list = SftpUtil.GetFilesList(path, SortMode);

		DefaultTableModel model = (DefaultTableModel)this.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);

		for(LsEntry entry : list) {
			String fn = entry.getFilename();
			int in = fn.lastIndexOf('.');
			String extn = (in != -1) ? fn.substring(in + 1) : "";
			Object row[] = {
					entry.getFilename(),
					String.valueOf(entry.getAttrs().getSize()),
					extn,
					String.valueOf(entry.getAttrs().getMTime() * 1000L)
					//dateFormat.format(new Date(entry.getAttrs().getMTime() * 1000L))
			};
			model.addRow(row);
		}
		
		Sort(SortMode);
	}
	
	public void Sort(int mode) {
		if (SortMode == mode)
			Asc = !Asc;
		else
			Asc = true;
		
		Vector mv = model.getDataVector();
		Collections.sort(mv, new ModelComparator(mode, Asc));
		
		SortMode = mode;
	}

	private String FileSize(String size) {
		double fSize = Double.parseDouble(size);
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

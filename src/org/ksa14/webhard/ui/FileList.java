package org.ksa14.webhard.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * FileList represents the GUI component that shows lists of files in the directory selected in DirectoryTree component 
 * 
 * @author Jongwook
 */
public class FileList extends JTable {
	public static final long serialVersionUID = 0L;
	
	private static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static final String[] COLUMNS = {"이름", "크기", "종류", "날짜"};
	public static final Object[][] ROWS = {};

	public static final int COLUMN_FILENAME		= 0;
	public static final int COLUMN_SIZE			= 1;
	public static final int COLUMN_EXT			= 2;
	public static final int COLUMN_DATE			= 3;

	protected DefaultTableModel model = new DefaultTableModel(FileList.ROWS, FileList.COLUMNS) {
		public static final long serialVersionUID = 0L;
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	
	protected class HeaderRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;

		private TableCellRenderer PrevRenderer;
		
		private int mode = COLUMN_FILENAME;
		private boolean asc = true;
				
		public HeaderRenderer setOriginalTableCellRenderer(TableCellRenderer tcr) {
			PrevRenderer = tcr;
			return this;
		}
		
		public void SetSort(int m, boolean a) {
			mode = m;
			asc = a;
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {	
			JLabel label = (JLabel)PrevRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (column == mode) {
				URL urlIcon = getClass().getResource("/res/sort_" + (asc ? "asc" : "desc") + ".png");
				label.setIcon(new ImageIcon(urlIcon));
				label.setHorizontalTextPosition(SwingConstants.LEFT);
			}
			
			return label;
		}
	}
	
	protected class HeaderMotionListener implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {
			// No dragging column header
			getTableHeader().setDraggedColumn(null);
		}
		
		public void mouseMoved(MouseEvent e) {}
	}

	protected static class ListRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		
		private static ListRenderer TheInstance;
		
		public static ListRenderer GetInstance() {
			return (TheInstance == null) ? TheInstance = new ListRenderer() : TheInstance;
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {		
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String text = value.toString();
			String ext = (String)table.getModel().getValueAt(row, FileList.COLUMN_EXT);
			
			if (column == 0){	// File name
				label.setText(text);
				label.setIcon(FileInfo.GetIcon(ext));
			} else {
				label.setIcon(null);
				if (column == 1) label.setText(((Long)value < 0) ? "" : FileSize((Long)value));	// File size
				if (column == 2) label.setText(FileInfo.GetDescription(text));					// File description
				if (column == 3) label.setText(DATEFORMAT.format(new Date((Long)value)));		// File modified date
			} 
			label.setHorizontalAlignment((column == 1) ? JLabel.RIGHT : JLabel.LEFT);
			return label;
		}
	}

	// Comparator for sorting file list
	protected static class ListComparator implements Comparator<Object> {		
		private static ListComparator[][] TheInstances;
		
		private final int mode;
		private final boolean asc;
		
		private ListComparator(int m, boolean a) {
			mode = m;
			asc = a;
		}
		
		public static ListComparator GetInstance(int m, boolean a) {
			if (TheInstances == null)
				TheInstances = new ListComparator[4][2];
			int j = a ? 0 : 1;
			return (TheInstances[m][j] == null) ? TheInstances[m][j] = new ListComparator(m, a) : TheInstances[m][j];
		}
		
		public int compare(Object arg0, Object arg1) {
			Vector<?> v0 = (Vector<?>)arg0;
			Vector<?> v1 = (Vector<?>)arg1;
			int sign = asc ? 1 : -1;

			if (mode == COLUMN_SIZE || mode == COLUMN_DATE) {
				long s0 = (Long)v0.elementAt(mode);
				long s1 = (Long)v1.elementAt(mode);
				return sign * ((s0 > s1) ? 1 : ((s0 < s1) ? -1 : 0));
			} else if (mode == COLUMN_FILENAME) {
				boolean s0 = (((String)v0.elementAt(COLUMN_EXT)).compareTo(".") == 0);
				boolean s1 = (((String)v1.elementAt(COLUMN_EXT)).compareTo(".") == 0);
				
				if (s0 ^ s1)
					return (s0 ? -sign : sign);
				else
					return sign * ((String)v0.elementAt(mode)).compareTo((String)v1.elementAt(mode));
			} else if (mode == COLUMN_EXT) {
				return sign * ((String)v0.elementAt(mode)).compareTo((String)v1.elementAt(mode));
			} 
			return 0;
		}
	}

	protected FileList() {
		super();
		
		setModel(model);
		setCellSelectionEnabled(true);
		setIntercellSpacing(new Dimension(3,3));
		setShowGrid(false);
		setRowHeight(20);

		JTableHeader header = getTableHeader();
		header.setDefaultRenderer((new HeaderRenderer()).setOriginalTableCellRenderer(header.getDefaultRenderer()));
		header.addMouseMotionListener(new HeaderMotionListener());
		
		setDefaultRenderer(Object.class, ListRenderer.GetInstance());
	}

	public void UpdateList(final String path) {}

	public void UpdateListDone(Vector<?> list, int mode, boolean asc) {}

	protected void Sort(int mode, boolean asc) {
		Vector<?> modelData = model.getDataVector();
		Collections.sort(modelData, ListComparator.GetInstance(mode, asc));
	}

	protected static String FileSize(long size) {
		float fsize = size;
		String[] units = {"B", "KB", "MB", "GB"};
		
		for (int i=0; i<units.length; ++i) {
			if (fsize < 1024.0) 
				return String.format("%.1f %s", fsize, units[i]);
			fsize /= 1024.0;
		}
		
		return String.format("%.1f TB", fsize);
	}
}

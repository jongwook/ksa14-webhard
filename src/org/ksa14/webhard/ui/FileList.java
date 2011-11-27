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

public class FileList extends JTable {
	public static final long serialVersionUID = 0L;
	
	private static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

	protected static final int COLUMN_FILENAME		= 0;
	protected static final int COLUMN_SIZE			= 1;
	protected static final int COLUMN_EXT			= 2;
	protected static final int COLUMN_DATE			= 3;

	protected final String[] COLUMNS = {"파일 이름", "크기", "종류", "날짜"};
	protected final Object[][] ROWS = {};
	
	protected class HeaderRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;

		private TableCellRenderer rendererPrev;
		
		private int mode = COLUMN_FILENAME;
		private boolean asc = true;
				
		public HeaderRenderer setOriginalTableCellRenderer(TableCellRenderer tcr) {
			rendererPrev = tcr;
			return this;
		}
		
		public void setSort(int m, boolean a) {
			mode = m;
			asc = a;
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {	
			JLabel label = (JLabel)rendererPrev.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (column == mode) {
				URL uicon = getClass().getResource("/res/sort_" + (asc ? "asc" : "desc") + ".png");
				label.setIcon(new ImageIcon(uicon));
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
		
		private static ListRenderer theInstance;
		
		public static ListRenderer GetInstance() {
			return (theInstance == null) ? (theInstance = new ListRenderer()) : theInstance;
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {		
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String text = value.toString();
			String ext = (String)table.getModel().getValueAt(row, FileList.COLUMN_EXT);
			
			switch (column) {
			case COLUMN_FILENAME:		// File name
				label.setIcon(FileUtility.getIcon(ext));	// File icon
				label.setText(text);
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_SIZE:		// File size
				label.setIcon(null);
				label.setText(((Long)value < 0) ? "" : FileUtility.getFileSize((Long)value));
				label.setHorizontalAlignment(JLabel.RIGHT);
				break;
			case COLUMN_EXT:		// File description
				label.setIcon(null);
				label.setText(FileUtility.getDescription(text));
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_DATE:		// File modified date
				label.setIcon(null);
				label.setText(DATEFORMAT.format(new Date((Long)value)));
				label.setHorizontalAlignment(JLabel.CENTER);
				break;
			default:
			}
			
			return label;
		}
	}

	// Comparator for sorting file list
	protected static class ListComparator implements Comparator<Object> {		
		private static ListComparator[][] theInstances;
		
		private final int mode;
		private final boolean asc;
		
		private ListComparator(int m, boolean a) {
			mode = m;
			asc = a;
		}
		
		public static ListComparator getInstance(int m, boolean a) {
			if (theInstances == null)
				theInstances = new ListComparator[4][2];
			int j = a ? 0 : 1;
			return (theInstances[m][j] == null) ? theInstances[m][j] = new ListComparator(m, a) : theInstances[m][j];
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
		
		setModel(new DefaultTableModel(ROWS, COLUMNS) {
			public static final long serialVersionUID = 0L;
			
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		setIntercellSpacing(new Dimension(0, 0));
		setShowGrid(false);
		setRowHeight(20);

		JTableHeader header = getTableHeader();
		header.setDefaultRenderer((new HeaderRenderer()).setOriginalTableCellRenderer(header.getDefaultRenderer()));
		header.addMouseMotionListener(new HeaderMotionListener());
		
		setDefaultRenderer(Object.class, ListRenderer.GetInstance());
	}
	
	protected void sort(int mode, boolean asc) {
		Vector<?> mdata = ((DefaultTableModel)getModel()).getDataVector();
		Collections.sort(mdata, ListComparator.getInstance(mode, asc));
	}	
}

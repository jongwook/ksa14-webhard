package org.ksa14.webhard.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

import org.ksa14.webhard.sftp.SftpAdapter;
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

	public static final int COLUMN_FILENAME		= 0;
	public static final int COLUMN_SIZE			= 1;
	public static final int COLUMN_EXT			= 2;
	public static final int COLUMN_DATE			= 3;

	public static int sortMode = COLUMN_FILENAME;
	public static boolean asc = true;

	/**
	 * The default table model 
	 */
	protected static DefaultTableModel model = new DefaultTableModel(FileList.rows, FileList.columns) {
		public static final long serialVersionUID = 0L;
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	public static class MyTableCellRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		private static MyTableCellRenderer theInstance;
		public static MyTableCellRenderer getInstance() {
			return (theInstance == null) ? theInstance = new MyTableCellRenderer() : theInstance;
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {			
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String text = value.toString();
			String extension = (String)table.getModel().getValueAt(row, FileList.COLUMN_EXT);
			
			if (column == 0){
				label.setText(text);
				label.setIcon(FileInfo.GetIcon(extension));
			} else {
				label.setIcon(null);				
				if (column == 1) label.setText(FileSize((Long)value));
				if (column == 2) label.setText(FileInfo.GetDescription(text));
				if (column == 3) label.setText(dateFormat.format(new Date((Long)value)));
			} 
			label.setHorizontalAlignment((column == 1)?JLabel.RIGHT:JLabel.LEFT);
			return label;
		}
	}

	public class ColumnHeaderListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTable table = FileList.GetInstance();
			int vColIndex = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (vColIndex != -1) 
				Sort(vColIndex, asc=(sortMode==vColIndex)?!asc:true);
		}
	}

	private static class ModelComparator implements Comparator<Object> {
		private int mode = 0;
		private boolean asc = true;
		private static ModelComparator instances[][];
		private ModelComparator(int m, boolean a) {
			this.mode = m;
			this.asc = a;
		}
		public static ModelComparator getInstance(int m, boolean a) {
			if(instances == null) instances = new ModelComparator[4][2];
			return (instances[m][a?0:1] == null) ? instances[m][a?0:1] = new ModelComparator(m, a) : instances[m][a?0:1];
		}
		public int compare(Object arg0, Object arg1) {
			Vector<?> v0 = (Vector<?>)arg0;
			Vector<?> v1 = (Vector<?>)arg1;
			int sign = asc?1:-1;

			if(mode == COLUMN_SIZE || mode == COLUMN_DATE) {
				long s0 = (Long)v0.elementAt(mode);
				long s1 = (Long)v1.elementAt(mode);
				return sign * ( (s0 > s1) ? 1 : ((s0 < s1) ? -1 : 0) );
			} else {
				return sign * ((String)v0.elementAt(mode)).compareTo((String)v1.elementAt(mode));
			}
		}
	}
	
	public class FileListListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				// double clicked
				JTable table = (JTable)e.getSource();
				TableModel model = table.getModel();
				int row = table.getSelectedRow();
				if(model.getValueAt(row, COLUMN_EXT).equals(".")) {
					// this is a folder
					DirectoryTree.GetInstance().ChangeDirectory((String)model.getValueAt(row, COLUMN_FILENAME));
				}
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
		this.setIntercellSpacing(new Dimension(3,3));
		this.setShowGrid(false);
		this.getColumnModel().getColumn(0).setPreferredWidth(400);
		this.setDefaultRenderer(Object.class, MyTableCellRenderer.getInstance());
		this.setRowHeight(20);

		this.getTableHeader().addMouseListener(new ColumnHeaderListener());
		this.addMouseListener(new FileListListener());
	}

	public static FileList GetInstance() {
		return (theInstance == null) ? theInstance = new FileList() : theInstance;
	}

	public void UpdateList(final String path) {
		new Thread() {
			public void run() {
				SftpAdapter.GetFilesList(path, sortMode);
			}
		}.start();
	}

	public void UpdateListDone(Vector<?> list) {	
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);

		for(Object obj : list) {
			LsEntry entry = (LsEntry)obj;
			String fn = entry.getFilename();
			int in = fn.lastIndexOf('.');
			String extension = (in != -1) ? fn.substring(in + 1) : "";
			Object row[] = {
					entry.getFilename(),
					new Long(entry.getAttrs().getSize()),
					(entry.getAttrs().isDir())?".":extension,
					new Long(entry.getAttrs().getMTime() * 1000L)
			};
			model.addRow(row);
		}

		Sort(COLUMN_FILENAME, true);
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.setEnabled(true);
		DirectoryTree.GetInstance().setEnabled(true);
	}

	private void Sort(int mode, boolean asc) {
		Vector<?> mv = model.getDataVector();
		Collections.sort(mv, ModelComparator.getInstance(mode, asc));
		sortMode = mode;
	}

	private static String FileSize(long size) {
		double fSize = size;
		String units[] = {"B", "KB", "MB", "GB"};
		for(int i=0; i<units.length; ++i) {
			if(fSize < 1024.0) 
				return String.format("%.1f %s", fSize, units[i]);
			fSize /= 1024.0;
		}
		return String.format("%.1f TB", fSize);
	}
}

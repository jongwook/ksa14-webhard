package org.ksa14.webhard.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpList;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * FileList represents the GUI component that shows lists of files in the directory selected in DirectoryTree component 
 * 
 * @author Jongwook
 */
public class FileList extends JTable implements MsgListener {
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
	public static boolean sortAsc = true;

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
			String ext = (String)table.getModel().getValueAt(row, FileList.COLUMN_EXT);
			
			if (column == 0){
				label.setText(text);
				label.setIcon(FileInfo.GetIcon(ext));
			} else {
				label.setIcon(null);
				if (column == 1) label.setText(((Long)value < 0) ? "" : FileSize((Long)value));
				if (column == 2) label.setText(FileInfo.GetDescription(text));
				if (column == 3) label.setText(dateFormat.format(new Date((Long)value)));
			} 
			label.setHorizontalAlignment((column == 1) ? JLabel.RIGHT : JLabel.LEFT);
			return label;
		}
	}
	
	public static class MyTableHeaderRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		
		private static MyTableHeaderRenderer theInstance;
		private TableCellRenderer prevRenderer;
		
		public static MyTableHeaderRenderer getInstance() {
			return (theInstance == null) ? theInstance = new MyTableHeaderRenderer() : theInstance;
		}
		
		public MyTableHeaderRenderer setOriginalTableCellRenderer(TableCellRenderer tcr) {
			prevRenderer = tcr;
			return this;
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {	
			JLabel label = (JLabel)prevRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (column == FileList.sortMode) {
				URL urlIcon = FileList.GetInstance().getClass().getResource("/res/sort_" + ((FileList.sortAsc) ? "asc" : "desc") + ".png");
				label.setIcon(new ImageIcon(urlIcon));
				label.setHorizontalTextPosition(SwingConstants.LEFT);
			}
			
			return label;
		}
	}
	
	public class ColumnHeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTable table = FileList.GetInstance();
			int vColIndex = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (vColIndex != -1) 
				Sort(vColIndex, (sortMode == vColIndex) ? !sortAsc : true);
		}
	}
	
	public class ColumnHeaderMotionListener implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {
			FileList.GetInstance().getTableHeader().setDraggedColumn(null);
		}
		
		public void mouseMoved(MouseEvent e) {}
	}
	
	public class FileListListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				// double clicked
				JTable table = (JTable)e.getSource();
				TableModel model = table.getModel();
				int row = table.getSelectedRow();
				if(model.getValueAt(row, COLUMN_EXT).equals("."))	// this is a folder
					DirectoryTree.GetInstance().ChangeDirectory((String)model.getValueAt(row, COLUMN_FILENAME));
			}
		}
	}

	// comparator for sorting file list
	private static class ModelComparator implements Comparator<Object> {
		private int mode = 0;
		private boolean asc = true;
		private static ModelComparator instances[][];
		
		private ModelComparator(int m, boolean a) {
			mode = m;
			asc = a;
		}
		
		public static ModelComparator getInstance(int m, boolean a) {
			if (instances == null) instances = new ModelComparator[4][2];
			int j = a ? 0 : 1;
			return (instances[m][j] == null) ? instances[m][j] = new ModelComparator(m, a) : instances[m][j];
		}
		
		public int compare(Object arg0, Object arg1) {
			Vector<?> v0 = (Vector<?>)arg0;
			Vector<?> v1 = (Vector<?>)arg1;
			int sign = asc?1:-1;

			if (mode == COLUMN_SIZE || mode == COLUMN_DATE) {
				long s0 = (Long)v0.elementAt(mode);
				long s1 = (Long)v1.elementAt(mode);
				return sign * ((s0 > s1) ? 1 : ((s0 < s1) ? -1 : 0));
			} else if (mode == COLUMN_FILENAME || mode == COLUMN_EXT) {
				return sign * ((String)v0.elementAt(mode)).compareTo((String)v1.elementAt(mode));
			} 
			return 0;
		}
	}

	/**
	 * Initializes the table and its columns
	 */
	private FileList() {
		super(FileList.model);

		setCellSelectionEnabled(true);
		setIntercellSpacing(new Dimension(3,3));
		setShowGrid(false);
		getColumnModel().getColumn(0).setPreferredWidth(400);
		setDefaultRenderer(Object.class, MyTableCellRenderer.getInstance());
		setRowHeight(20);

		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(MyTableHeaderRenderer.getInstance().setOriginalTableCellRenderer(getTableHeader().getDefaultRenderer()));
		header.addMouseMotionListener(new ColumnHeaderMotionListener());
		header.addMouseListener(new ColumnHeaderMouseListener());
		addMouseListener(new FileListListener());
		
		MsgBroadcaster.AddListener(this);
	}

	public static FileList GetInstance() {
		return (theInstance == null) ? theInstance = new FileList() : theInstance;
	}

	public void UpdateList(final String path) {
		new Thread() {
			public void run() {
				SftpList.GetFilesList(path);
			}
		}.start();
	}

	public void UpdateListDone(Vector<?> list) {	
		DefaultTableModel model = (DefaultTableModel)getModel();
		model.setRowCount(0);

		Iterator<?> listI = list.iterator();
		while (listI.hasNext()) {
			LsEntry entry = (LsEntry)listI.next();
			String fileName = entry.getFilename();
			
			// Skip hidden files
			if (fileName.charAt(0) == '.')
				continue;
			
			int indexExt = fileName.lastIndexOf('.');
			String extension = (indexExt != -1) ? fileName.substring(indexExt + 1) : "";
			Object row[] = {
					entry.getFilename(),
					(entry.getAttrs().isDir()) ? -1 : new Long(entry.getAttrs().getSize()),
					(entry.getAttrs().isDir()) ? "." : extension,
					new Long(entry.getAttrs().getMTime() * 1000L)
			};
			model.addRow(row);
		}

		Sort(sortMode, sortAsc);
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		setEnabled(true);
	}

	private void Sort(int mode, boolean asc) {
		Vector<?> modelData = model.getDataVector();
		Collections.sort(modelData, ModelComparator.getInstance(mode, asc));
		
		sortMode = mode;
		sortAsc = asc;
	}

	private static String FileSize(long size) {
		double fSize = size;
		String units[] = {"B", "KB", "MB", "GB"};
		
		for (int i=0; i<units.length; ++i) {
			if (fSize < 1024.0) 
				return String.format("%.1f %s", fSize, units[i]);
			fSize /= 1024.0;
		}
		
		return String.format("%.1f TB", fSize);
	}

	@Override
	public void ReceiveMsg(final int type, final Object arg) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.FILELIST_DONE)
					GetInstance().UpdateListDone((Vector<?>)arg);
				
				if (type == MsgListener.FILELIST_FAIL)
					GetInstance().UpdateListDone(new Vector<Object>());
			}
		});
	}
}

package org.ksa14.webhard.ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpList;
import org.ksa14.webhard.sftp.SftpList.PathLsEntry;

public class SearchFileList extends FileList implements MsgListener {
	private static final long serialVersionUID = 0L;

	private static SearchFileList TheInstance;
	public static int SortMode = FileList.COLUMN_FILENAME;
	public static boolean SortAsc = true;
	
	private class HeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTable table = SearchFileList.GetInstance();
			int icol = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (icol != -1)  {
				if (SearchFileList.SortMode == icol)
					SearchFileList.SortAsc = !SearchFileList.SortAsc;
				else
					SearchFileList.SortAsc = true;
				SearchFileList.SortMode = icol;

				((HeaderRenderer)SearchFileList.GetInstance().getTableHeader().getDefaultRenderer()).SetSort(SearchFileList.SortMode, SearchFileList.SortAsc);
				Sort(SearchFileList.SortMode, SearchFileList.SortAsc);
			}
			
			table.getSelectionModel().clearSelection();
		}
	}
	
	private class ListMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				// When double clicked
				JTable table = (JTable)e.getSource();
				TableModel model = table.getModel();
				int row = table.getSelectedRow();
				if (model.getValueAt(row, COLUMN_EXT).equals(".")) {	// If this is a folder
					DirectoryTree.GetInstance().UpdateTree((String)model.getValueAt(row, 4));
					MsgBroadcaster.BroadcastMsg(PANEL_EXPLORE, null);
				}
			}
		}
	}
	
	private SearchFileList() {
		super();
		
		getTableHeader().addMouseListener(new HeaderMouseListener());		
		addMouseListener(new ListMouseListener());
		
		model.addColumn("path");
		getColumnModel().removeColumn(getColumnModel().getColumn(4));
		getColumnModel().getColumn(0).setPreferredWidth(500);
		
		MsgBroadcaster.AddListener(this);
	}

	public static SearchFileList GetInstance() {
		return (TheInstance == null) ? TheInstance = new SearchFileList() : TheInstance;
	}

	public void UpdateList(final String sword) {
		new Thread() {
			public void run() {
				SftpList.GetSearchFilesList(sword);
			}
		}.start();
	}

	public void UpdateListDone(Vector<?> list, int mode, boolean asc) {
		DefaultTableModel model = (DefaultTableModel)getModel();
		model.setRowCount(0);
		
		Iterator<?> listI = list.iterator();
		while (listI.hasNext()) {
			PathLsEntry pe = (PathLsEntry)listI.next();
			String filename = pe.entry.getFilename();
			
			int indexExt = filename.lastIndexOf('.');
			String extension = (indexExt != -1) ? filename.substring(indexExt + 1) : "";
			Object[] row = {
					filename,
					(pe.entry.getAttrs().isDir()) ? -1 : new Long(pe.entry.getAttrs().getSize()),
					(pe.entry.getAttrs().isDir()) ? "." : extension,
					new Long(pe.entry.getAttrs().getMTime() * 1000L),
					pe.path
			};
			model.addRow(row);
		}

		Sort(mode, asc);
		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		setEnabled(true);
	}
	
	public void ReceiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.SEARCH_DONE)
					GetInstance().UpdateListDone((Vector<?>)arg, SearchFileList.SortMode, SearchFileList.SortAsc);
				
				if (type == MsgListener.SEARCH_FAIL)
					GetInstance().UpdateListDone(new Vector<Object>(), SearchFileList.SortMode, SearchFileList.SortAsc);
			}
		});
	}
}

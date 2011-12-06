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
import org.ksa14.webhard.sftp.SftpList.SearchEntry;

public class SearchFileList extends FileList  implements MsgListener {
	private static final long serialVersionUID = 0L;

	public static final int COLUMN_PATH	= 4;
	
	private static SearchFileList theInstance;
	
	public static int sortMode = FileList.COLUMN_FILENAME;
	public static boolean sortAsc = true;
	
	private class HeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// When header clicked
			JTable table = getInstance();
			int icol = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (icol != -1)  {
				if (SearchFileList.sortMode == icol)
					SearchFileList.sortAsc = !SearchFileList.sortAsc;
				else
					SearchFileList.sortAsc = true;
				SearchFileList.sortMode = icol;
				
				((HeaderRenderer)getInstance().getTableHeader().getDefaultRenderer()).setSort(SearchFileList.sortMode, SearchFileList.sortAsc);
				sort(SearchFileList.sortMode, SearchFileList.sortAsc);
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
				String dirpath = (String)model.getValueAt(row, COLUMN_PATH);
				if (model.getValueAt(row, COLUMN_EXT).equals("."))	// If this is a directory
					dirpath += "/" + (String)model.getValueAt(row, COLUMN_FILENAME);
				ExploreDirectoryTree.getInstance().changePath(dirpath);
				MsgBroadcaster.broadcastMsg(PANEL_EXPLORE, null);
			}
		}
	}

	private SearchFileList() {
		super();
		
		getTableHeader().addMouseListener(new HeaderMouseListener());
		addMouseListener(new ListMouseListener());

		((DefaultTableModel)getModel()).addColumn("path");
		getColumnModel().removeColumn(getColumnModel().getColumn(COLUMN_PATH));
		
		getColumnModel().getColumn(COLUMN_FILENAME).setPreferredWidth(450);
		getColumnModel().getColumn(COLUMN_DATE).setMinWidth(75);
		getColumnModel().getColumn(COLUMN_DATE).setMaxWidth(75);
		getColumnModel().getColumn(COLUMN_DATE).setPreferredWidth(75);
		
		MsgBroadcaster.addListener(this);
	}

	public static SearchFileList getInstance() {
		return (theInstance == null) ? (theInstance = new SearchFileList()) : theInstance;
	}

	public void updateList(final String sword) {
		if (sword.trim().length() == 0) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "검색어를 입력해주세요");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "검색어를 입력해주세요");
			return;
		}
		
		setEnabled(false);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		new Thread() {
			public void run() {
				((DefaultTableModel)getModel()).setRowCount(0);
				SftpList.getSearchFilesList(sword);
			}
		}.start();
	}
	
	public void updateListDone(Vector<?> filelist) {
		DefaultTableModel model = (DefaultTableModel)getModel();
		model.setRowCount(0);
		
		Iterator<?> fileiter = filelist.iterator();
		while (fileiter.hasNext()) {
			SearchEntry fileentry = (SearchEntry)fileiter.next();
			
			int iExt = fileentry.filename.lastIndexOf('.');
			String fileext = (iExt != -1) ? fileentry.filename.substring(iExt + 1) : "";
			model.addRow(new Object[] {
					fileentry.filename,
					(fileentry.isdir) ? -1 : fileentry.filesize,
					(fileentry.isdir) ? "." : fileext,
					fileentry.mtime * 1000L,
					fileentry.path
			});
		}
		
		this.sort(sortMode, sortAsc);
		
		setEnabled(true);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.SEARCH_DONE)
					getInstance().updateListDone((Vector<?>)arg);
				
				if (type == MsgListener.SEARCH_FAIL)
					getInstance().updateListDone(new Vector<Object>());
			}
		});
	}
}

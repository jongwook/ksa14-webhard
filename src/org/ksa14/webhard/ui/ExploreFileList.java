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

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class ExploreFileList extends FileList implements MsgListener {
	private static final long serialVersionUID = 0L;

	private static ExploreFileList TheInstance;
	public static int SortMode = FileList.COLUMN_FILENAME;
	public static boolean SortAsc = true;
	
	private class HeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTable table = ExploreFileList.GetInstance();
			int icol = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (icol != -1)  {
				if (ExploreFileList.SortMode == icol)
					ExploreFileList.SortAsc = !ExploreFileList.SortAsc;
				else
					ExploreFileList.SortAsc = true;
				ExploreFileList.SortMode = icol;
				
				((HeaderRenderer)ExploreFileList.GetInstance().getTableHeader().getDefaultRenderer()).SetSort(ExploreFileList.SortMode, ExploreFileList.SortAsc);
				Sort(ExploreFileList.SortMode, ExploreFileList.SortAsc);
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
				if (model.getValueAt(row, COLUMN_EXT).equals("."))	// If this is a folder
					DirectoryTree.GetInstance().ChangeDirectory((String)model.getValueAt(row, COLUMN_FILENAME));
			}
		}
	}
	
	private ExploreFileList() {
		super();
		
		getTableHeader().addMouseListener(new HeaderMouseListener());
		addMouseListener(new ListMouseListener());
		
		getColumnModel().getColumn(0).setPreferredWidth(400);
		
		MsgBroadcaster.AddListener(this);
	}

	public static ExploreFileList GetInstance() {
		return (TheInstance == null) ? TheInstance = new ExploreFileList() : TheInstance;
	}

	public void UpdateList(final String path) {
		new Thread() {
			public void run() {
				SftpList.GetExploreFilesList(path);
			}
		}.start();
	}

	public void UpdateListDone(Vector<?> list, int mode, boolean asc) {
		DefaultTableModel model = (DefaultTableModel)getModel();
		model.setRowCount(0);
		
		Iterator<?> listi = list.iterator();
		while (listi.hasNext()) {
			LsEntry entry = (LsEntry)listi.next();
			String filename = entry.getFilename();
			
			int indexExt = filename.lastIndexOf('.');
			String extension = (indexExt != -1) ? filename.substring(indexExt + 1) : "";
			Object[] row = {
					filename,
					(entry.getAttrs().isDir()) ? -1 : new Long(entry.getAttrs().getSize()),
					(entry.getAttrs().isDir()) ? "." : extension,
					new Long(entry.getAttrs().getMTime() * 1000L),
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
				if (type == MsgListener.FILELIST_DONE)
					GetInstance().UpdateListDone((Vector<?>)arg, ExploreFileList.SortMode, ExploreFileList.SortAsc);
				
				if (type == MsgListener.FILELIST_FAIL)
					GetInstance().UpdateListDone(new Vector<Object>(), ExploreFileList.SortMode, ExploreFileList.SortAsc);
			}
		});
	}
}

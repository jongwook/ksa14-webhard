package org.ksa14.webhard.ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpList;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;

public class ExploreFileList extends FileList implements MsgListener {
	private static final long serialVersionUID = 0L;
	
	private static ExploreFileList theInstance;
	
	public static int sortMode = FileList.COLUMN_FILENAME;
	public static boolean sortAsc = true;
	
	private class HeaderMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// When header clicked
			JTable table = getInstance();
			int icol = table.getColumnModel().getColumnIndexAtX(e.getX());

			if (icol != -1)  {
				if (ExploreFileList.sortMode == icol)
					ExploreFileList.sortAsc = !ExploreFileList.sortAsc;
				else
					ExploreFileList.sortAsc = true;
				ExploreFileList.sortMode = icol;
				
				((HeaderRenderer)getInstance().getTableHeader().getDefaultRenderer()).setSort(ExploreFileList.sortMode, ExploreFileList.sortAsc);
				sort(ExploreFileList.sortMode, ExploreFileList.sortAsc);
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
				if (model.getValueAt(row, COLUMN_EXT).equals("."))	// If this is a directory
					ExploreDirectoryTree.getInstance().changePathChild((String)model.getValueAt(row, COLUMN_FILENAME));
				else
					MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_CLICK, null);
			}
		}
	}

	private ExploreFileList() {
		super();
		
		getTableHeader().addMouseListener(new HeaderMouseListener());
		addMouseListener(new ListMouseListener());
		
		getColumnModel().getColumn(COLUMN_FILENAME).setPreferredWidth(450);
		getColumnModel().getColumn(COLUMN_DATE).setMinWidth(75);
		getColumnModel().getColumn(COLUMN_DATE).setMaxWidth(75);
		getColumnModel().getColumn(COLUMN_DATE).setPreferredWidth(75);
		
		MsgBroadcaster.addListener(this);
	}
	
	public static ExploreFileList getInstance() {
		return (theInstance == null) ? (theInstance = new ExploreFileList()) : theInstance;
	}

	public void updateList(final String path) {
		setEnabled(false);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		new Thread() {
			public void run() {
				((DefaultTableModel)getModel()).setRowCount(0);
				SftpList.getExploreFilesList(path);
			}
		}.start();
	}

	public void updateListDone(Vector<?> filelist) {
		DefaultTableModel model = (DefaultTableModel)getModel();
		model.setRowCount(0);
		
		Iterator<?> fileiter = filelist.iterator();
		while (fileiter.hasNext()) {
			LsEntry fileentry = (LsEntry)fileiter.next();
			String filename = fileentry.getFilename();
			SftpATTRS fileattr = fileentry.getAttrs();
			
			int iExt = filename.lastIndexOf('.');
			String fileext = (iExt != -1) ? filename.substring(iExt + 1) : "";
			model.addRow(new Object[] {
					filename,
					(fileattr.isDir()) ? -1 : fileattr.getSize(),
					(fileattr.isDir()) ? "." : fileext,
					fileentry.getAttrs().getMTime() * 1000L
			});
		}
		
		this.sort(sortMode, sortAsc);
		
		setEnabled(true);
		WebhardFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.FILELIST_DONE)
					getInstance().updateListDone((Vector<?>)arg);
				
				if (type == MsgListener.FILELIST_FAIL)
					getInstance().updateListDone(new Vector<Object>());
			}
		});
	}
}

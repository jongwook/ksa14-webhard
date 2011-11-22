package org.ksa14.webhard.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.TransferFileData;

public class TransferList extends JTable {	
	public static final long serialVersionUID = 0L;

	protected final String[] COLUMNS = {"파일 이름", "전송 속도", "남은 시간", "진행상황", "", ""};
	protected final Object[][] ROWS = {};
	protected final Object[] NULLROW = {"", "", "", "", "", ""};

	protected static final int COLUMN_FILENAME		= 0;
	protected static final int COLUMN_SPEED			= 1;
	protected static final int COLUMN_TIME			= 2;
	protected static final int COLUMN_STATUS			= 3;
	protected static final int COLUMN_PAUSE			= 4;
	protected static final int COLUMN_STOP			= 5;
	
	protected Vector<TransferFileData> TransferFile = new Vector<TransferFileData>();

	protected static class ListRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {			
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String text = value.toString();
			
			switch (column) {
			case COLUMN_FILENAME:		// File name
				int iext = text.lastIndexOf('.');
				String ext = (iext != -1) ? text.substring(iext + 1) : "";
				label.setIcon(FileInfo.GetIcon(ext));
				label.setText(text);
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_SPEED:		// Transfer speed
				label.setIcon(null);
				break;
			case COLUMN_TIME:		// Remaining time
				label.setIcon(null);
				break;
			case COLUMN_STATUS:		// Transfer status
				label.setIcon(null);
				break;
			case COLUMN_PAUSE:		// Transfer pause button
				label.setIcon(null);
				label.setText(text);
				if (text.equals("pause"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_pause.png")));
				else if (text.equals("resume"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_resume.png")));
				break;
			case COLUMN_STOP:		// Transfer stop button
				label.setIcon(null);
				label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_stop.png")));
				break;
			default:
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
	
	private class ListMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			DefaultTableModel model = (DefaultTableModel)getModel();
			int row = rowAtPoint(e.getPoint());
			int col = columnAtPoint(e.getPoint());
			
			if (col == COLUMN_PAUSE) {
				/*
				if (model.getValueAt(row, col).equals("pause"))
					model.setValueAt("resume", row, col);
				else
					model.setValueAt("pause", row, col);
				*/
			} else if (col == COLUMN_STOP) {
			}
		}
	}
	
	protected TransferList() {
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
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setShowGrid(false);
		setShowHorizontalLines(true);
		setGridColor(Color.decode("#d0d0d0"));
		setRowHeight(20);
		
		setDefaultRenderer(Object.class, new ListRenderer());

		getTableHeader().addMouseMotionListener(new HeaderMotionListener());
		
		addMouseListener(new ListMouseListener());

		getColumnModel().getColumn(COLUMN_FILENAME).setPreferredWidth(300);
		getColumnModel().getColumn(COLUMN_SPEED).setPreferredWidth(50);
		getColumnModel().getColumn(COLUMN_TIME).setPreferredWidth(80);
		getColumnModel().getColumn(COLUMN_STATUS).setPreferredWidth(120);
		getColumnModel().getColumn(COLUMN_PAUSE).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setPreferredWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setPreferredWidth(20);
		
		new Thread() {
			public void run() {
				while (true) {
					UpdateList();
					try {this.sleep(1);} catch (InterruptedException e) {}
				}
			}
		}.start();
	}
	
	protected void UpdateList() {
		final DefaultTableModel model = (DefaultTableModel)getModel();
		
		int r = 0;
		while (r < TransferFile.size()) {
			TransferFileData filedata = TransferFile.get(r);
			if (filedata.Mode == SftpTransfer.MODE_FINISH) {
				TransferFile.remove(r);
				model.removeRow(r);
			} else {
				float rtime = (filedata.Size - filedata.Count) / filedata.Speed;
				model.setValueAt(FileSize(filedata.Speed) + "/s", r, COLUMN_SPEED);
				model.setValueAt(TimeFormat(rtime), r, COLUMN_TIME);
				model.setValueAt(FileSize(filedata.Count) + "/" + FileSize(filedata.Size), r, COLUMN_STATUS);
				model.setValueAt((filedata.Mode == SftpTransfer.MODE_RUNNING) ? "pause" : "resume", r, COLUMN_PAUSE);
				r++;
			}
		}
	}

	protected static String FileSize(long size) {
		return FileSize((float)size);
	}
	
	protected static String FileSize(float size) {
		String[] units = {"B", "KB", "MB", "GB"};
		
		for (int i=0; i<units.length; i++) {
			if (size < 1024.0f) 
				return String.format("%.1f %s", size, units[i]);
			size /= 1024.0f;
		}
		
		return String.format("%.1f TB", size);
	}
	
	protected static String TimeFormat(float time) {
		long ltime = (long)time;
		int h, m, s;
		String stime = "";

		if (time > 0)
			ltime += 1;
		h = (int)ltime / 3600;
		if (h > 0)
			stime += h + "시간 ";
		ltime %= 3600;
		m = (int)ltime / 60;
		if (m > 0)
			stime += m + "분 ";
		s = (int)ltime % 60;
		stime += s + "초";
		
		return stime;
	}
}

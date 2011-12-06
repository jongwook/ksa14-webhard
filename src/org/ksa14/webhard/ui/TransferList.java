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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;

public class TransferList extends JTable {
	public static final long serialVersionUID = 0L;

	protected final String[] COLUMNS = {"파일 이름", "전송 속도", "남은 시간", "진행상황", "", ""};
	protected final Object[][] ROWS = {};

	protected static final int COLUMN_FILENAME		= 0;
	protected static final int COLUMN_SPEED			= 1;
	protected static final int COLUMN_TIME			= 2;
	protected static final int COLUMN_STATUS			= 3;
	protected static final int COLUMN_PAUSE			= 4;
	protected static final int COLUMN_STOP			= 5;
	
	protected Vector<SftpTransferData> fileTransfer = new Vector<SftpTransferData>();
	
	protected static class ListRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 0L;
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {			
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String text = value.toString();
			
			switch (column) {
			case COLUMN_FILENAME:		// File name
				int iext = text.lastIndexOf('.');
				String ext = (iext != -1) ? text.substring(iext + 1) : "";
				label.setIcon(FileUtility.getIcon(ext));
				label.setText(text);
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_SPEED:		// Transfer speed
			case COLUMN_TIME:		// Remaining time
			case COLUMN_STATUS:		// Transfer status
				label.setIcon(null);
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_PAUSE:		// Transfer pause button
				label.setIcon(null);
				label.setText(text);
				if (text.equals("pause"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_pause.png")));
				else if (text.equals("resume"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_resume.png")));
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_STOP:		// Transfer stop button
				label.setIcon(null);
				if (text.equals("stop"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_stop.png")));
				label.setHorizontalAlignment(JLabel.LEFT);
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
	
	protected class ListMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			DefaultTableModel model = (DefaultTableModel)getModel();
			int row = rowAtPoint(e.getPoint());
			int col = columnAtPoint(e.getPoint());
			
			if (col == COLUMN_PAUSE) {
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
		getColumnModel().getColumn(COLUMN_TIME).setPreferredWidth(50);
		getColumnModel().getColumn(COLUMN_STATUS).setPreferredWidth(150);
		getColumnModel().getColumn(COLUMN_PAUSE).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setPreferredWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setPreferredWidth(20);
	}
	
	protected void updateList(SftpTransferData filedata) {
		final DefaultTableModel model = (DefaultTableModel)getModel();
		
		int filerow = fileTransfer.indexOf(filedata);
		if (filerow < 0)
			return;
		
		switch (filedata.mode) {
		case SftpTransfer.MODE_NONE:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt("", filerow, COLUMN_STATUS);
			model.setValueAt("", filerow, COLUMN_PAUSE);
			model.setValueAt("", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_RUNNING:
			float rtime = (filedata.fileSize - filedata.fileSizeDone) / filedata.fileSpeed;
			model.setValueAt(FileUtility.getFileSize(filedata.fileSpeed) + "/s", filerow, COLUMN_SPEED);
			model.setValueAt(FileUtility.getTimeString(rtime), filerow, COLUMN_TIME);
			model.setValueAt(FileUtility.getFileSize(filedata.fileSizeDone) + "/" + FileUtility.getFileSize(filedata.fileSize) + " (" + String.format("%.1f", filedata.fileSizeDone * 100.0f / filedata.fileSize) + "%)", filerow, COLUMN_STATUS);
			model.setValueAt("pause", filerow, COLUMN_PAUSE);
			model.setValueAt("stop", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_PAUSED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt("일시 정지", filerow, COLUMN_STATUS);
			model.setValueAt("resume", filerow, COLUMN_PAUSE);
			model.setValueAt("stop", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_FINISHED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt("전송 완료", filerow, COLUMN_STATUS);
			model.setValueAt("", filerow, COLUMN_PAUSE);
			model.setValueAt("", filerow, COLUMN_STOP);
			break;
		}
	}
}

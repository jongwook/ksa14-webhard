package org.ksa14.webhard.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.ksa14.webhard.sftp.SftpTransfer;
import org.ksa14.webhard.sftp.SftpTransfer.SftpTransferData;

public class TransferList extends JTable {
	public static final long serialVersionUID = 0L;

	protected final String[] COLUMNS = {"파일 이름", "전송 속도", "남은 시간", "파일 크기", "진행상황", "", ""};
	protected final Object[][] ROWS = {};

	protected static final int COLUMN_FILENAME		= 0;
	protected static final int COLUMN_SPEED			= 1;
	protected static final int COLUMN_TIME			= 2;
	protected static final int COLUMN_SIZE			= 3;
	protected static final int COLUMN_STATUS			= 4;
	protected static final int COLUMN_PAUSE			= 5;
	protected static final int COLUMN_STOP			= 6;
	
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
				label.setIcon(null);
				label.setHorizontalAlignment(JLabel.RIGHT);
				label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				break;
			case COLUMN_TIME:		// Remaining time
				label.setIcon(null);
				label.setHorizontalAlignment(JLabel.RIGHT);
				label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				break;
			case COLUMN_SIZE:		// Remaining time
				label.setIcon(null);
				label.setHorizontalAlignment(JLabel.RIGHT);
				label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				break;
			case COLUMN_STATUS:		// Transfer status
				label.setIcon(null);
				label.setHorizontalAlignment(JLabel.LEFT);
				label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				if (text.equals("ready")) {
					label.setText("전송 대기");
				} else if (text.equals("started")) {
					label.setText("전송 준비중");
				} else if (text.equals("paused")) {
					label.setText("전송 일시정지");
				} else if (text.equals("stopped")) {
					label.setText("전송 중지");
				} else if (text.equals("finished")) {
					label.setText("전송 완료");
				} else {
					float prog = Float.parseFloat(text);
					label.setText(prog + "%");
					
				}
				break;
			case COLUMN_PAUSE:		// Transfer pause button
				label.setIcon(null);
				if (text.equals("pause"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_pause.png")));
				else if (text.equals("resume"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_resume.png")));
				label.setText("");
				label.setHorizontalAlignment(JLabel.LEFT);
				break;
			case COLUMN_STOP:		// Transfer stop button
				label.setIcon(null);
				if (text.equals("stop"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_stop.png")));
				else if (text.equals("delete"))
					label.setIcon(new ImageIcon(getClass().getResource("/res/transfer_delete.png")));
				label.setText("");
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
			String text = (String)model.getValueAt(row, col);
			
			if (col == COLUMN_PAUSE) {
				if (text.equals("pause"))
					pauseTransfer(row);
				else if (text.equals("resume"))
					fileTransfer.get(row).mode = SftpTransfer.MODE_NONE;
			} else if (col == COLUMN_STOP) {
				if (text.equals("stop")) {
					fileTransfer.get(row).mode = SftpTransfer.MODE_STOPPED;
					updateList(row);
				} else if (text.equals("delete")) {
					removeRow(row);
				}
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
		getColumnModel().getColumn(COLUMN_SPEED).setPreferredWidth(30);
		getColumnModel().getColumn(COLUMN_TIME).setPreferredWidth(80);
		getColumnModel().getColumn(COLUMN_SIZE).setPreferredWidth(50);
		getColumnModel().getColumn(COLUMN_STATUS).setPreferredWidth(100);
		getColumnModel().getColumn(COLUMN_PAUSE).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_PAUSE).setPreferredWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMaxWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setMinWidth(20);
		getColumnModel().getColumn(COLUMN_STOP).setPreferredWidth(20);
		
		new Thread() {
			public void run() {
				while (true) {
					startNextTransfer();
					try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		}.start();
	}
	
	protected void updateList(SftpTransferData filedata) {
		updateList(filedata, fileTransfer.indexOf(filedata));
	}
	
	protected void updateList(int filerow) {
		updateList(fileTransfer.get(filerow), filerow);
	}
	
	protected void updateList(SftpTransferData filedata, int filerow) {
		if ((filedata == null) || (filerow < 0))
			return;
		
		final DefaultTableModel model = (DefaultTableModel)getModel();
		
		switch (filedata.mode) {
		case SftpTransfer.MODE_NONE:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt("", filerow, COLUMN_SIZE);
			model.setValueAt("ready", filerow, COLUMN_STATUS);
			model.setValueAt("", filerow, COLUMN_PAUSE);
			model.setValueAt("delete", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_STARTED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt("", filerow, COLUMN_SIZE);
			model.setValueAt("started", filerow, COLUMN_STATUS);
			model.setValueAt("pause", filerow, COLUMN_PAUSE);
			model.setValueAt("stop", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_RUNNING:
			float rtime = (filedata.fileSize - filedata.fileSizeDone) / filedata.fileSpeed;
			model.setValueAt(FileUtility.getFileSize(filedata.fileSpeed) + "/s", filerow, COLUMN_SPEED);
			model.setValueAt(FileUtility.getTimeString(rtime), filerow, COLUMN_TIME);
			model.setValueAt(FileUtility.getFileSize(filedata.fileSizeDone) + "/" + FileUtility.getFileSize(filedata.fileSize), filerow, COLUMN_SIZE);
			model.setValueAt(String.format("%.1f", filedata.fileSizeDone * 100.0f / filedata.fileSize), filerow, COLUMN_STATUS);
			model.setValueAt("pause", filerow, COLUMN_PAUSE);
			model.setValueAt("stop", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_PAUSED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt(FileUtility.getFileSize(filedata.fileSizeDone) + "/" + FileUtility.getFileSize(filedata.fileSize), filerow, COLUMN_SIZE);
			model.setValueAt("paused", filerow, COLUMN_STATUS);
			model.setValueAt("resume", filerow, COLUMN_PAUSE);
			model.setValueAt("stop", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_STOPPED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt(FileUtility.getFileSize(filedata.fileSizeDone) + "/" + FileUtility.getFileSize(filedata.fileSize), filerow, COLUMN_SIZE);
			model.setValueAt("stopped", filerow, COLUMN_STATUS);
			model.setValueAt("", filerow, COLUMN_PAUSE);
			model.setValueAt("delete", filerow, COLUMN_STOP);
			break;
		case SftpTransfer.MODE_FINISHED:
			model.setValueAt("", filerow, COLUMN_SPEED);
			model.setValueAt("", filerow, COLUMN_TIME);
			model.setValueAt(FileUtility.getFileSize(filedata.fileSizeDone) + "/" + FileUtility.getFileSize(filedata.fileSize), filerow, COLUMN_SIZE);
			model.setValueAt("finished", filerow, COLUMN_STATUS);
			model.setValueAt("", filerow, COLUMN_PAUSE);
			model.setValueAt("delete", filerow, COLUMN_STOP);
			break;
		}
	}
	
	protected void removeRow(int row) {
		if (row < 0)
			return;
		
		fileTransfer.remove(row);
		((DefaultTableModel)getModel()).removeRow(row);
	}
	
	protected void removeRow(SftpTransferData filedata) {
		removeRow(fileTransfer.indexOf(filedata));
	}
	
	protected void startNextTransfer() {
		int runcnt = 0;
		int filecnt = fileTransfer.size();
		
		for (int i=0; i<filecnt; i++) {
			int filemode = fileTransfer.get(i).mode;
			if ((filemode == SftpTransfer.MODE_STARTED) || (filemode == SftpTransfer.MODE_RUNNING) || (filemode == SftpTransfer.MODE_PAUSED)) {
				runcnt++;
				if (runcnt >= SftpTransfer.MAX_TRANSFER)
					return;
			}
		}
		
		for (int i=0; i<filecnt; i++) {
			SftpTransferData filedata = fileTransfer.get(i);
			if (filedata.mode == SftpTransfer.MODE_NONE) {
				filedata.mode = SftpTransfer.MODE_STARTED;
				filedata.thread.start();
				updateList(filedata);
				runcnt++;
				if (runcnt >= SftpTransfer.MAX_TRANSFER)
					return;
			}
		}
	}
	
	protected void pauseTransfer(int row) {}
}

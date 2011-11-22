package org.ksa14.webhard.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

public class WebhardToolBar extends JToolBar {
	public static final long serialVersionUID = 0L;
	
	JTextField TextSearch;
	
	public class WebhardToolBarButton extends JButton implements ActionListener {
		private static final long serialVersionUID = 0L;
		private final Insets margins = new Insets(2, 2, 2, 2);
		
		public WebhardToolBarButton(URL urlIcon, String label, String action) {
			super();
			
			setMargin(margins);
			setVerticalTextPosition(CENTER);
			setHorizontalTextPosition(RIGHT);
			
			setIcon(new ImageIcon(urlIcon));
			setText(label);
			setActionCommand(action);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("exit"))
				WebhardFrame.Exit();
			if (e.getActionCommand().equals("explorepane"))
				MsgBroadcaster.BroadcastMsg(MsgListener.PANEL_EXPLORE, null);
			if (e.getActionCommand().equals("searchpane"))
				MsgBroadcaster.BroadcastMsg(MsgListener.PANEL_SEARCH, null);
			if (e.getActionCommand().equals("transferpane"))
				MsgBroadcaster.BroadcastMsg(MsgListener.PANEL_TRANSFER, null);
			if (e.getActionCommand().equals("download"))
				MsgBroadcaster.BroadcastMsg(MsgListener.DOWNLOAD_CLICK, null);
			if (e.getActionCommand().equals("upload"))
				MsgBroadcaster.BroadcastMsg(MsgListener.UPLOAD_CLICK, null);
			
			if (e.getActionCommand().equals("search")) {
				new Thread() {
					public void run() {
						SearchFileList.GetInstance().UpdateList(TextSearch.getText());
					}
				}.start();
			}
		}
	}
	
	private Separator NewSep(int width, int height, int orientation) {
		Separator sep = new Separator(new Dimension(width, height));
		sep.setOrientation(orientation);
		return sep;
	}
	
	public WebhardToolBar() {
		setLayout(new BorderLayout());
		
		add(NewSep(0, 4, SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		add(NewSep(0, 4, SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		
		Box tools = Box.createHorizontalBox();
		tools.add(NewSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/exit.png"), "종료", "exit"));
		tools.add(NewSep(15, 24, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/explore.png"), "웹하드", "explorepane"));
		tools.add(NewSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/searchp.png"), "검색결과", "searchpane"));
		tools.add(NewSep(15, 24, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/download.png"), "다운로드", "download"));
		tools.add(NewSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/upload.png"), "업로드", "upload"));
		tools.add(NewSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new WebhardToolBarButton(getClass().getResource("/res/transfer.png"), "전송상황", "transferpane"));
		add(tools, BorderLayout.WEST);
		
		Box search = Box.createHorizontalBox();
		search.add(new JLabel("파일 검색 : "));
		TextSearch = new JTextField();
		TextSearch.setPreferredSize(new Dimension(150, 16));
		TextSearch.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					SearchFileList.GetInstance().UpdateList(TextSearch.getText());
				}
			}
		});
		search.add(TextSearch);
		search.add(new WebhardToolBarButton(getClass().getResource("/res/search.png"), "", "search"));
		search.add(new Separator(new Dimension(5, 0)));
		add(search, BorderLayout.EAST);
		
		setFloatable(false);
	}
}

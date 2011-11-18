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
			
			if (e.getActionCommand().equals("search")) {
				new Thread() {
					public void run() {
						SearchFileList.GetInstance().UpdateList(TextSearch.getText());
					}
				}.start();
			}
		}
	}
	
	private Separator newSeparator(int width, int height, int orientation) {
		Separator nSep = new Separator(new Dimension(width, height));
		nSep.setOrientation(orientation);
		return nSep;
	}
	
	public WebhardToolBar() {
		setLayout(new BorderLayout());
		
		add(newSeparator(0, 4, SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		add(newSeparator(0, 4, SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		
		Box boxButton = Box.createHorizontalBox();
		boxButton.add(newSeparator(5, 0, SwingConstants.VERTICAL));
		boxButton.add(new WebhardToolBarButton(getClass().getResource("/res/exit.png"), "종료", "exit"));
		boxButton.add(newSeparator(15, 24, SwingConstants.VERTICAL));
		boxButton.add(new WebhardToolBarButton(getClass().getResource("/res/explore.png"), "웹하드", "explorepane"));
		boxButton.add(newSeparator(5, 0, SwingConstants.VERTICAL));
		boxButton.add(new WebhardToolBarButton(getClass().getResource("/res/searchp.png"), "검색결과", "searchpane"));
		boxButton.add(newSeparator(15, 24, SwingConstants.VERTICAL));
		add(boxButton, BorderLayout.WEST);
		
		Box boxSearch = Box.createHorizontalBox();
		TextSearch = new JTextField();
		TextSearch.setPreferredSize(new Dimension(150, 16));
		TextSearch.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					SearchFileList.GetInstance().UpdateList(TextSearch.getText());
				}
			}
		});
		boxSearch.add(TextSearch);
		boxSearch.add(new WebhardToolBarButton(getClass().getResource("/res/search.png"), "검색", "search"));
		boxSearch.add(new Separator(new Dimension(5, 0)));
		add(boxSearch, BorderLayout.EAST);
		
		setFloatable(false);
	}
}

package org.ksa14.webhard.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

public class WebhardToolBar extends JToolBar {
	public static final long serialVersionUID = 0L;
	
	JTextField textSearch;
	
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
		}
	}
	
	private Separator newSeparator(int width, int height, int orientation) {
		Separator nSep = new Separator(new Dimension(width, height));
		nSep.setOrientation(orientation);
		return nSep;
	}
	
	public WebhardToolBar() {
		setLayout(new BorderLayout());
		
		add(newSeparator(0, 2, SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		add(newSeparator(0, 2, SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		
		Box boxButton = Box.createHorizontalBox();
		boxButton.add(newSeparator(2, 0, SwingConstants.VERTICAL));
		boxButton.add(new WebhardToolBarButton(getClass().getResource("/res/exit.png"), "종료", "exit"));
		boxButton.add(newSeparator(10, 24, SwingConstants.VERTICAL));
		add(boxButton, BorderLayout.WEST);
		
		Box boxSearch = Box.createHorizontalBox();
		textSearch = new JTextField();
		textSearch.setPreferredSize(new Dimension(150, 16));
		boxSearch.add(textSearch);
		boxSearch.add(new WebhardToolBarButton(getClass().getResource("/res/search.png"), "검색", "search"));
		boxSearch.add(new Separator(new Dimension(2, 0)));
		add(boxSearch, BorderLayout.EAST);
		
		setFloatable(false);
	}
}

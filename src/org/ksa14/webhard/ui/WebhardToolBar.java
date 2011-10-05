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
			
			this.setMargin(margins);
			this.setVerticalTextPosition(CENTER);
			this.setHorizontalTextPosition(RIGHT);
			
			this.setIcon(new ImageIcon(urlIcon));
			this.setText(label);
			this.setActionCommand(action);
			this.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("exit")) {
				if (JOptionPane.showOptionDialog(null, "웹하드를 종료합니다", "KSA14 Webhard Client", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0)
					System.exit(0);
			}
		}
	}
	
	private Separator newSeparator(int width, int height, int orientation) {
		Separator nSep = new Separator(new Dimension(width, height));
		nSep.setOrientation(orientation);
		return nSep;
	}
	
	public WebhardToolBar() {
		this.setLayout(new BorderLayout());
		
		this.add(newSeparator(0, 2, SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		this.add(newSeparator(0, 2, SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		
		Box boxButton = Box.createHorizontalBox();
		boxButton.add(newSeparator(2, 0, SwingConstants.VERTICAL));
		boxButton.add(new WebhardToolBarButton(getClass().getResource("/res/exit.png"), "종료", "exit"));
		boxButton.add(newSeparator(10, 24, SwingConstants.VERTICAL));
		this.add(boxButton, BorderLayout.WEST);
		
		Box boxSearch = Box.createHorizontalBox();
		textSearch = new JTextField();
		textSearch.setPreferredSize(new Dimension(150, 16));
		boxSearch.add(textSearch);
		boxSearch.add(new WebhardToolBarButton(getClass().getResource("/res/search.png"), "검색", "search"));
		boxSearch.add(new Separator(new Dimension(2, 0)));
		this.add(boxSearch, BorderLayout.EAST);
		
		this.setFloatable(false);
	}
}

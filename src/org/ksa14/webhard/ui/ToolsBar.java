package org.ksa14.webhard.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpList;

public class ToolsBar extends JToolBar {
	public static final long serialVersionUID = 0L;
	
	public static ToolsBar theInstance;
	
	private JTextField textSearch;
	
	public class ToolsBarButton extends JButton implements ActionListener {
		private static final long serialVersionUID = 0L;
		private final Insets margins = new Insets(2, 2, 2, 2);
		
		public ToolsBarButton(URL urlIcon, String label, String action) {
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
			if (e.getActionCommand().equals("connect"))
				WebhardFrame.authenticate();
			
			if (e.getActionCommand().equals("exit"))
				WebhardFrame.exit();
			
			if (e.getActionCommand().equals("explorepane"))
				MsgBroadcaster.broadcastMsg(MsgListener.PANEL_EXPLORE, null);
			if (e.getActionCommand().equals("searchpane"))
				MsgBroadcaster.broadcastMsg(MsgListener.PANEL_SEARCH, null);
			if (e.getActionCommand().equals("transferpane"))
				MsgBroadcaster.broadcastMsg(MsgListener.PANEL_TRANSFER, null);

			if (e.getActionCommand().equals("download")) {
				MsgBroadcaster.broadcastMsg(MsgListener.DOWNLOAD_CLICK, null);
				MsgBroadcaster.broadcastMsg(MsgListener.PANEL_DOWNLOAD, null);
			}

			if (e.getActionCommand().equals("upload")) {
				MsgBroadcaster.broadcastMsg(MsgListener.UPLOAD_CLICK, null);
				MsgBroadcaster.broadcastMsg(MsgListener.PANEL_UPLOAD, null);
			}
			
			if (e.getActionCommand().equals("search"))
				doSearch();

			if (e.getActionCommand().equals("newdirectory")) {
				if (ExploreDirectoryTree.getInstance().getPath().equals("/")) {
					MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "최상위 폴더에는 새로운 폴더를 만들 수 없습니다.");
				} else {
					final String dirname = JOptionPane.showInputDialog(null, "폴더 이름을 입력해주세요", "새 폴더", JOptionPane.PLAIN_MESSAGE);
					
					if (dirname != null) {
						new Thread() {
							public void run() {
								SftpList.createDirectory(ExploreDirectoryTree.getInstance().getPath(), dirname);
							}
						}.start();
					}
				}
			}
		}
	}
	
	public ToolsBar() {
		setLayout(new BorderLayout());
		
		add(newSep(0, 4, SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		add(newSep(0, 4, SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		
		Box tools = Box.createHorizontalBox();
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/connect.png"), "재접속", "connect"));
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/exit.png"), "종료", "exit"));
		tools.add(newSep(15, 24, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/explore.png"), "웹하드", "explorepane"));
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/searchp.png"), "검색결과", "searchpane"));
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/transfer.png"), "전송상황", "transferpane"));
		tools.add(newSep(15, 24, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/download.png"), "다운로드", "download"));
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/upload.png"), "업로드", "upload"));
		tools.add(newSep(5, 0, SwingConstants.VERTICAL));
		tools.add(new ToolsBarButton(getClass().getResource("/res/newdir.png"), "새 폴더", "newdirectory"));
		add(tools, BorderLayout.WEST);
		
		Box boxSearch = Box.createHorizontalBox();
		boxSearch.add(new JLabel("파일 검색 : "));
		textSearch = new JTextField();
		textSearch.setPreferredSize(new Dimension(150, 16));
		textSearch.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
					doSearch();
			}
		});
		boxSearch.add(textSearch);
		boxSearch.add(new ToolsBarButton(getClass().getResource("/res/search.png"), "", "search"));
		boxSearch.add(new Separator(new Dimension(5, 0)));
		add(boxSearch, BorderLayout.EAST);
		
		setFloatable(false);
	}
	
	private Separator newSep(int width, int height, int orientation) {
		Separator sep = new Separator(new Dimension(width, height));
		sep.setOrientation(orientation);
		return sep;
	}
	
	private void doSearch() {
		new Thread() {
			public void run() {
				SearchFileList.getInstance().updateList(textSearch.getText());
			}
		}.start();
		MsgBroadcaster.broadcastMsg(MsgListener.PANEL_SEARCH, null);
	}

	public static ToolsBar getInstance() {
		return (theInstance == null) ? (theInstance = new ToolsBar()) : theInstance;
	}
}

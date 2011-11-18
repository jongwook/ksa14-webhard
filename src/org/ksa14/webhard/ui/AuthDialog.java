package org.ksa14.webhard.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.sftp.SftpAdapter;

public class AuthDialog extends JDialog implements MsgListener {
	private static final long serialVersionUID = 0L;
	public static final int wWidth = 460;
	public static final int wHeight = 200;

	private JTextField textID;
	private JPasswordField textPW;
	private JButton btnConnect, btnExit;
	private StatusBarLabel statusBar;
	private static boolean authed = false;

	public AuthDialog() {
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();

		setLayout(new BorderLayout());
		
		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(null);
		loginPanel.setBackground(Color.white);

		// Load KSA logo image
		ImageIcon iconLogo;
		URL urlLogo = getClass().getResource("/res/ksa.jpg");
		if (urlLogo != null) {
			iconLogo = new ImageIcon(urlLogo);

			JLabel labelLogo = new JLabel("");
			labelLogo.setBounds(25, 25, iconLogo.getIconWidth(), iconLogo.getIconHeight());
			labelLogo.setIcon(iconLogo);
			loginPanel.add(labelLogo);
		}

		// Add components to input login information
		JLabel labelID = new JLabel("학번");
		labelID.setBounds(195, 30, 60, 15);
		loginPanel.add(labelID);

		JLabel labelPW = new JLabel("비밀번호");
		labelPW.setBounds(195, 70, 60, 15);
		loginPanel.add(labelPW);

		textID = new JTextField();
		textID.setBounds(260, 27, 155, 21);
		textID.setColumns(20);
		loginPanel.add(textID);

		textPW = new JPasswordField();
		textPW.setBounds(260, 67, 155, 21);
		textPW.setColumns(20);
		textPW.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					RequestAuth(textID.getText(), new String(textPW.getPassword()));
				}
			}
		});
		loginPanel.add(textPW);

		// Add buttons to connect or exit
		btnConnect = new JButton("접속");
		btnConnect.setBounds(290, 115, 60, 25);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RequestAuth(textID.getText(), new String(textPW.getPassword()));
			}
		});
		loginPanel.add(btnConnect);

		btnExit = new JButton("종료");
		btnExit.setBounds(355, 115, 60, 25);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				authed = false;
				dispose();
			}
		});
		loginPanel.add(btnExit);

		add(loginPanel, BorderLayout.CENTER);

		// Add the status bar
		statusBar = new StatusBarLabel("KSA14 Webhard");
		add(statusBar, BorderLayout.SOUTH);

		// Set window properties
		setTitle("KSA14 Webhard Login");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Set window size and location
		int sW = (int)getToolkit().getScreenSize().getWidth();
		int sH = (int)getToolkit().getScreenSize().getHeight();
		setSize(wWidth, wHeight);
		setLocation((sW - wWidth) / 2, (sH - wHeight) / 2);
		setResizable(false);
		
		// Add message listener to listen broadcast message
		MsgBroadcaster.AddListener(this);
		MsgBroadcaster.AddListener(statusBar);

		// Show window
		setVisible(true);
	}
	
	public void dispose() {
		// Remove message listener from broadcaster
		MsgBroadcaster.RemoveListener(this);
		MsgBroadcaster.RemoveListener(statusBar);
		super.dispose();
	}
	
	public static boolean IsAuth() {
		return authed;
	}
	
	public void RequestAuth(final String id, final String pw) {
		// Check if id and password is given
		if ((id.trim().length() == 0) || (pw.trim().length() == 0)) {
			JOptionPane.showMessageDialog(null, "학번과 비밀번호를 입력해주세요", "KSA14 Webhard Login", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		btnConnect.setEnabled(false);

		// Try to authenticate
		new Thread() {
			public void run() {
				SftpAdapter.Connect(id, pw);
			}
		}.start();
	}

	@Override
	public void ReceiveMsg(final int type, final Object arg) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.CONNECT_SUCCESS) {
					authed = true;
					dispose();
				}
				
				if (type == MsgListener.CONNECT_FAIL) {
					authed = false;
					JOptionPane.showMessageDialog(null, "서버 접속에 실패하였습니다", "KSA14 Webhard Login", JOptionPane.ERROR_MESSAGE);
					btnConnect.setEnabled(true);
				}
			}
		});
	}
}

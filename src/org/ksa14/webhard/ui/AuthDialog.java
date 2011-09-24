package org.ksa14.webhard.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import org.ksa14.webhard.sftp.*;

public class AuthDialog extends JDialog {
	private static final long serialVersionUID = 0L;
	public static final int wWidth = 460;
	public static final int wHeight = 200;
	
	private JTextField textID;
	private JPasswordField textPW;
	private static boolean authed = false;
	
	public static boolean Authenticate() {
		new AuthDialog();
		return authed;
	}

	public AuthDialog() {
		// Set absolute layout
		this.setLayout(null);

		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();
		this.setBackground(Color.white);
		
		// Load KSA logo image
		ImageIcon IconLogo;
		URL UrlLogo = getClass().getResource("/res/ksa.jpg");
		if (UrlLogo != null) {
			IconLogo = new ImageIcon(UrlLogo);
			
			JLabel LabelLogo = new JLabel("");
			LabelLogo.setBounds(25, 25, IconLogo.getIconWidth(), IconLogo.getIconHeight());
			LabelLogo.setIcon(IconLogo);
			this.add(LabelLogo);
		}
		
		// Add components to input login information
		JLabel LabelID = new JLabel("학번");
		LabelID.setBounds(195, 30, 60, 15);
		this.add(LabelID);
		
		JLabel LabelPW = new JLabel("비밀번호");
		LabelPW.setBounds(195, 70, 60, 15);
		this.add(LabelPW);
		
		textID = new JTextField();
		textID.setBounds(260, 27, 155, 21);
		textID.setColumns(20);
		this.add(textID);
		
		textPW = new JPasswordField();
		textPW.setBounds(260, 67, 155, 21);
		textPW.setColumns(20);
		textPW.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					authed = RequestAuth(textID.getText(), new String(textPW.getPassword()));
					if (authed)
						dispose();
				}
			}
		});
		this.add(textPW);
		
		// Add buttons to connect or exit
		JButton BtnConnect = new JButton("접속");
		BtnConnect.setBounds(290, 115, 60, 25);
		BtnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				authed = RequestAuth(textID.getText(), new String(textPW.getPassword()));
				if (authed)
					dispose();
			}
		});
		this.add(BtnConnect);
		
		JButton BtnExit = new JButton("종료");
		BtnExit.setBounds(355, 115, 60, 25);
		BtnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				authed = false;
				dispose();
			}
		});
		this.add(BtnExit);
		
		// Set window properties
		this.setTitle("KSA14 Webhard Login");
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Set window size and location
		int sW = (int)getToolkit().getScreenSize().getWidth();
		int sH = (int)getToolkit().getScreenSize().getHeight();
		this.setSize(wWidth, wHeight);
		this.setLocation((sW - wWidth) / 2, (sH - wHeight) / 2);
		this.setResizable(false);
		
		// Show window
		this.setVisible(true);
	}
	
	public boolean RequestAuth(String id, String pw) {
		// Check if id and password is given
		if ((id.trim().length() == 0) || (pw.trim().length() == 0)) {
			JOptionPane.showMessageDialog(null, "학번과 비밀번호를 입력해주세요", "KSA14 Webhard Login", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		// Try to authenticate
		if (!SftpUtil.Connect(id, pw)) {
			JOptionPane.showMessageDialog(null, "접속에 실패하였습니다", "KSA14 Webhard Login", JOptionPane.ERROR_MESSAGE);
			return false;	// Fail
		}
		
		// Success
		return true;
	}
}

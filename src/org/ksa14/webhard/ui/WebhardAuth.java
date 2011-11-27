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

public class WebhardAuth extends JDialog implements MsgListener {
	private static final long serialVersionUID = 0L;

	public static WebhardAuth theInstance;

	private final int WIDTH = 460;
	private final int HEIGHT = 190;

	private JTextField textID;
	private JPasswordField textPW;
	private JButton btnConnect, btnExit;
	
	public static boolean authed;
	
	private WebhardAuth() {
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();

		setLayout(new BorderLayout());

		// Set window properties
		setTitle("KSA14 Webhard Login");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		// Main panel
		JPanel panelAuth = new JPanel();
		panelAuth.setLayout(null);
		panelAuth.setBackground(Color.white);

		// Load KSA logo image
		ImageIcon iconLogo;
		URL urlLogo = getClass().getResource("/res/ksa.jpg");
		if (urlLogo != null) {
			iconLogo = new ImageIcon(urlLogo);

			JLabel logolabel = new JLabel("");
			logolabel.setBounds(25, 25, iconLogo.getIconWidth(), iconLogo.getIconHeight());
			logolabel.setIcon(iconLogo);
			panelAuth.add(logolabel);
		}

		// Components for input login information
		JLabel labelID = new JLabel("학번");
		labelID.setBounds(195, 30, 60, 15);
		panelAuth.add(labelID);

		JLabel labelPW = new JLabel("비밀번호");
		labelPW.setBounds(195, 70, 60, 15);
		panelAuth.add(labelPW);

		textID = new JTextField();
		textID.setBounds(260, 27, 155, 21);
		textID.setColumns(20);
		panelAuth.add(textID);

		textPW = new JPasswordField();
		textPW.setBounds(260, 67, 155, 21);
		textPW.setColumns(20);
		textPW.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					requestAuth(textID.getText(), new String(textPW.getPassword()));
				}
			}
		});
		panelAuth.add(textPW);

		// Add buttons to connect and exit
		btnConnect = new JButton("접속");
		btnConnect.setBounds(290, 115, 60, 25);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestAuth(textID.getText(), new String(textPW.getPassword()));
			}
		});
		panelAuth.add(btnConnect);

		btnExit = new JButton("취소");
		btnExit.setBounds(355, 115, 60, 25);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				authed = false;
				dispose();
			}
		});
		panelAuth.add(btnExit);

		add(panelAuth, BorderLayout.CENTER);

		// Set window size and location
		int sw = (int)getToolkit().getScreenSize().getWidth();
		int sh = (int)getToolkit().getScreenSize().getHeight();
		setSize(WIDTH, HEIGHT);
		setLocation((sw - WIDTH) / 2, (sh - HEIGHT) / 2);
		setResizable(false);
	}
	
	public static WebhardAuth GetInstance() {
		return (theInstance == null) ? theInstance = new WebhardAuth() : theInstance;
	}
	
	public static void open() {
		authed = false;
		WebhardAuth dialog = GetInstance();
		
		dialog.textID.setText("");
		dialog.textPW.setText("");
		dialog.btnConnect.setEnabled(true);
		
		// Add to message broadcaster
		MsgBroadcaster.addListener(dialog);
		
		// Show dialog
		dialog.setVisible(true);
	}
	
	public void dispose() {
		// Remove message listener from broadcaster
		MsgBroadcaster.removeListener(this);
				
		super.dispose();
	}
		
	public void requestAuth(final String id, final String pw) {
		// Check if id and password is given
		if ((id.trim().length() == 0) || (pw.trim().length() == 0)) {
			JOptionPane.showMessageDialog(null, "학번과 비밀번호를 입력해주세요", "KSA14 Webhard Login", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		btnConnect.setEnabled(false);

		// Try to connect
		new Thread() {
			public void run() {
				SftpAdapter.connect(id, pw);
			}
		}.start();
	}

	public void receiveMsg(final int type, final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.CONNECT_SUCCESS) {
					authed = true;
					dispose();
				}
				
				if (type == MsgListener.CONNECT_FAIL) {
					authed = false;
					btnConnect.setEnabled(true);
				}
			}
		});
	}
}

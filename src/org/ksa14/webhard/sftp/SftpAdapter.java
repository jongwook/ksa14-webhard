package org.ksa14.webhard.sftp;

import java.util.Properties;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpAdapter {
	public static final String USERNAME = "apache";
	public static final String HOST = "webhard.ksa14.org";

	private static JSch jschSftp = new JSch();
	private static Session sessionMainSftp = null;
	private static ChannelSftp channelMainSftp = null;
	private static final Properties config = new Properties();
	
	public static void connect(String id, String pw) {
		if (isConnected())
			disconnect();
		
		try {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "로그인 정보를 확인하는 중입니다");

			// Get SSH public, private keys from server
			String[] SSHKeys = SftpUtility.getKeys(id);
			if (SSHKeys == null)
				throw new Exception();

			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버에 접속중입니다");

			// Init Jsch
			String hpw = SftpUtility.md5(pw);
			jschSftp.addIdentity("Key", SSHKeys[1].getBytes(), SSHKeys[0].getBytes(), hpw.getBytes());
			
			// Start SSH session
			sessionMainSftp = jschSftp.getSession(USERNAME, HOST, 22);
			
			config.put("StrictHostKeyChecking", "no");
			sessionMainSftp.setConfig(config);

			sessionMainSftp.connect();
			
			// Connect to Sftp channel
			channelMainSftp = (ChannelSftp)sessionMainSftp.openChannel("sftp");
			channelMainSftp.connect();
		} catch (Exception e) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버 접속에 실패하였습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "서버 접속에 실패하였습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.CONNECT_FAIL, null);
			return;
		}
		
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되었습니다");
		MsgBroadcaster.broadcastMsg(MsgListener.CONNECT_SUCCESS, null);
		return;
	}
	
	public static boolean isConnected() {
		return ((channelMainSftp != null) && channelMainSftp.isConnected());
	}
	
	public static void disconnect() {
		if (isConnected()) {
			sessionMainSftp.disconnect();
			channelMainSftp.disconnect();
			sessionMainSftp = null;
			channelMainSftp = null;
		}
	}
	
	public static ChannelSftp getMainChannel() {
		return channelMainSftp;
	}
	
	public static ChannelSftp getNewChannel() throws JSchException {
		// Set new Sftp channel connection
		Session session = jschSftp.getSession(USERNAME, HOST, 22);
		session.setConfig(config);
		session.connect();
		
		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		channel.connect();
		
		return channel;
	}
}

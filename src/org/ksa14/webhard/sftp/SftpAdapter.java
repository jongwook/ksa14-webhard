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

	private static JSch SftpJsch;
	private static Session SftpSession;
	private static boolean Connected = false;
	
	public static void Connect(String id, String pw) {
		try {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "로그인 정보를 확인하는 중입니다");

			// Get SSH public, private keys from server
			String[] SSHKeys = AuthUtil.GetKeys(id);
			if (SSHKeys == null)
				throw new Exception();

			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속중입니다");

			// Init Jsch
			SftpJsch = new JSch();
			String hpw = AuthUtil.md5(pw);
			SftpJsch.addIdentity("Key", SSHKeys[1].getBytes(), SSHKeys[0].getBytes(), hpw.getBytes());
			
			// Start SSH session
			SftpSession = SftpJsch.getSession(USERNAME, HOST, 22);
			
			Properties conf = new Properties();
			conf.put("StrictHostKeyChecking", "no");
			SftpSession.setConfig(conf);

			SftpSession.connect();
		} catch (Exception e) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버 접속에 실패하였습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_FAIL, null);
			return;
		}

		Connected = true;
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "KSA14 Webhard 에 접속되었습니다");
		MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_SUCCESS, null);
		return;
	}
	
	public static boolean IsConnected() {
		return Connected;
	}
	
	public static ChannelSftp GetNewChannel() {
		ChannelSftp channel;
		try {
			channel = (ChannelSftp)SftpSession.openChannel("sftp");
			channel.connect();
		} catch (JSchException e) {
			return null;
		}
		return channel;
	}
}

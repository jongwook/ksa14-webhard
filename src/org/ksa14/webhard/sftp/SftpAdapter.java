package org.ksa14.webhard.sftp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpAdapter {
	public static final String USERNAME = "apache";
	public static final String HOST = "webhard.ksa14.org";

	private static JSch jsch;
	private static Session session;
	private static HashMap<String, ChannelSftp> channels = new HashMap<String, ChannelSftp>();
	private static boolean connected = false;
	
	public static void Connect(String id, String pw) {
		try {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "로그인 정보를 확인하는 중입니다");

			// Get SSH public, private keys from server
			String[] SSHKeys = AuthUtil.GetKeys(id);
			if (SSHKeys == null)
				throw new Exception();

			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "세션을 초기화합니다");

			// Init Jsch
			jsch = new JSch();
			String hpw = AuthUtil.md5(pw);
			jsch.addIdentity("Key", SSHKeys[1].getBytes(), SSHKeys[0].getBytes(), hpw.getBytes());
			
			// Start SSH session
			session = jsch.getSession(USERNAME, HOST, 22);
			
			Properties conf = new Properties();
			conf.put("StrictHostKeyChecking", "no");
			session.setConfig(conf);

			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속중입니다");

			session.connect();
			
			// Start SFTP channel
			ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
			channels.put("main", channel);
			channel.connect();
			channel.setFilenameEncoding("UTF-8");
		} catch (Exception e) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버 접속에 실패하였습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_FAIL, null);
			return;
		}

		connected = true;
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "KSA14 Webhard 에 접속되었습니다");
		MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_SUCCESS, null);
		return;
	}
	
	public static boolean IsConnected() {
		return connected;
	}
	
	public static void Disconnect() {
		synchronized (channels) {
			Iterator<String> iter = channels.keySet().iterator();
			while (iter.hasNext())
				channels.get(iter.next()).disconnect();
		}
		session.disconnect();
	}
	
	public static ChannelSftp getChannel(String key) {
		return channels.get(key);
	}
}

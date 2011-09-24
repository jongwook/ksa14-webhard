package org.ksa14.webhard.sftp;

import java.util.*;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.*;

public class SftpAdapter extends SftpPublisher {
	public static final String Username = "apache";
	public static final String Host = "webhard.ksa14.org";

	private static JSch jsch;
	private static Session session;
	private static HashMap<String, ChannelSftp> channels = new HashMap<String, ChannelSftp>();

	public static boolean Connect(String id, String pw) {
		try {
			// Get SSH public, private keys from server
			UpdateStatus(SftpListener.INFO, "로그인 정보를 확인하는 중입니다");
			String[] SshKeys = AuthUtil.GetKeys(id);
			if (SshKeys == null)
				return false;

			// Init Jsch
			UpdateStatus(SftpListener.INFO, "세션 초기화 중");
			jsch = new JSch();
			String hpw = AuthUtil.md5(pw);
			jsch.addIdentity("Key", SshKeys[1].getBytes(), SshKeys[0].getBytes(), hpw.getBytes());

			// Start SSH session
			session = jsch.getSession(Username, Host, 22);
			
			Properties conf = new Properties();
			conf.put("StrictHostKeyChecking", "no");
			session.setConfig(conf);

			UpdateStatus(SftpListener.INFO, "서버에 접속중입니다");
			session.connect();

			// Start SFTP channel
			UpdateStatus(SftpListener.INFO, "세션을 시작합니다");
			ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
			channels.put("main", channel);
			channel.connect();
			channel.setFilenameEncoding("UTF-8");
		} catch (Exception e) {
			UpdateStatus(SftpListener.FAILED, "접속에 실패했습니다");
			return false;
		}

		UpdateStatus(SftpListener.SUCCEED, "접속에 성공헀습니다");
		RemoveAllListeners();
		return true;
	}

	public static void GetDirectoryList(String path) {
		ChannelSftp channel = channels.get("main");
		synchronized(channel) {
			// Check connection
			if (!channel.isConnected())
				return;
	
			Vector<String> retv = new Vector<String>();
	
			try {
				UpdateStatus(SftpListener.INFO, "디렉토리를 탐색중입니다 : " + path);
				Vector<?> lsv = channel.ls(path);
				for (int i=0; i<lsv.size(); i++) {
					Object tmp = lsv.elementAt(i);
					if (tmp instanceof LsEntry) {
						LsEntry tmpe = (LsEntry)tmp;
						String dirname = tmpe.getFilename();
						if ((dirname.compareTo(".") != 0) && (dirname.compareTo("..") != 0) && (dirname.compareTo("recycle_bin") != 0) && tmpe.getAttrs().isDir())
							retv.add(dirname);
					}
				}
			} catch (SftpException e) {
				UpdateStatus(SftpListener.FAILED, "파일탐색에 실패했습니다 : " + e.getMessage());
				retv = null;
			}
			
			Collections.sort(retv, String.CASE_INSENSITIVE_ORDER);
			UpdateStatus(SftpListener.INFO, "완료");
			UpdateStatus(SftpListener.DIRLIST_DONE, retv);
		}
	}

	public static void GetFilesList(String path, int sortmode) {
		ChannelSftp channel = channels.get("main");
		synchronized(channel) {
			// Check connection
			if (!channel.isConnected())
				return;
	
			Vector<LsEntry> retv = new Vector<LsEntry>();
	
			try {
				UpdateStatus(SftpListener.INFO, "디렉토리를 탐색중입니다 : " + path);
				Vector<?> lsv = channel.ls(path);
				for (int i=0; i<lsv.size(); i++) {
					Object item = lsv.elementAt(i);
					if (item instanceof LsEntry) {
						LsEntry entry = (LsEntry)item;
						String filename = entry.getFilename();
						if ((filename.compareTo(".") != 0) && (filename.compareTo("..") != 0) && (filename.compareTo("recycle_bin") != 0))
							retv.add(entry);
					}
				}
			} catch (SftpException e) {
				UpdateStatus(SftpListener.FAILED, "파일탐색에 실패했습니다 : " + e.getMessage());
				retv = null;
			}
			
			UpdateStatus(SftpListener.INFO, "완료");
			UpdateStatus(SftpListener.FILELIST_DONE, retv);
		}
	}
}

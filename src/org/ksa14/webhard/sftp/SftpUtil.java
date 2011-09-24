package org.ksa14.webhard.sftp;

import java.util.*;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.*;

public class SftpUtil {
	public static final String Username = "apache";
	public static final String Host = "webhard.ksa14.org";

	private static JSch SUJsch;
	private static Session SUSession;
	private static ChannelSftp SUChannel;

	public static boolean Connect(String id, String pw) {
		try {
			// Get SSH public, private keys from server
			String[] SshKeys = AuthUtil.GetKeys(id);
			if (SshKeys == null)
				return false;

			// Init Jsch
			SUJsch = new JSch();
			String hpw = AuthUtil.md5(pw);
			SUJsch.addIdentity("Key", SshKeys[1].getBytes(), SshKeys[0].getBytes(), hpw.getBytes());

			// Start SSH session
			SUSession = SUJsch.getSession(Username, Host, 22);
			Properties SUConf = new Properties();
			SUConf.put("StrictHostKeyChecking", "no");
			SUSession.setConfig(SUConf);

			SUSession.connect();

			// Start SFTP channel
			SUChannel = (ChannelSftp)SUSession.openChannel("sftp");
			SUChannel.connect();
			SUChannel.setFilenameEncoding("UTF-8");
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public static Vector<String> GetDirectoryList(String path) {
		// Check connection
		if (!SUChannel.isConnected())
			return null;

		Vector<String> retv = new Vector<String>();

		try {
			Vector<?> lsv = SUChannel.ls(path);
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
			e.printStackTrace();
			retv = null;
		}
		
		Collections.sort(retv, String.CASE_INSENSITIVE_ORDER);
		return retv;
	}

	public static Vector<LsEntry> GetFilesList(String path, int sortmode) {
		// Check connection
		if (!SUChannel.isConnected())
			return null;

		Vector<LsEntry> retv = new Vector<LsEntry>();

		try {
			Vector<?> lsv = SUChannel.ls(path);
			for (int i=0; i<lsv.size(); i++) {
				Object item = lsv.elementAt(i);
				if (item instanceof LsEntry) {
					LsEntry entry = (LsEntry)item;
					String filename = entry.getFilename();
					if ((filename.compareTo(".") != 0) && (filename.compareTo("..") != 0) && (filename.compareTo("recycle_bin") != 0) && !entry.getAttrs().isDir())
						retv.add(entry);
				}
			}
		} catch (SftpException e) {
			e.printStackTrace();
			retv = null;
		}
		
		return retv;
	}
}

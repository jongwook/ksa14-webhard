package org.ksa14.webhard.sftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;
import org.ksa14.webhard.ui.ExploreDirectoryTree;

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpList {
	public static class SearchEntry {
		public String path = "";
		public String filename = "";
		public boolean isdir = false;
		public long filesize = 0;
		public int mtime = 0;
	}
	
	public static void getDirectoryList(String path) {
		// Check connection
		if (!SftpAdapter.isConnected()) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.DIRTREE_FAIL, null);
			return;
		}
		
		ChannelSftp channel = SftpAdapter.getMainChannel();
		
		// Get directory list from sftp
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "디렉토리를 탐색중입니다");
		
		Vector<String> dirlist = new Vector<String>();
		synchronized(channel) {
			try {
				Vector<?> lslist = channel.ls(path);
				Iterator<?> lsiter = lslist.iterator();
				while (lsiter.hasNext()) {
					LsEntry lsentry = (LsEntry)lsiter.next();
					// Add only directories. Skip hidden files
					String filename = ((LsEntry)lsentry).getFilename();
					if ((filename.charAt(0) != '.') && !filename.equals("recycle_bin") && ((LsEntry)lsentry).getAttrs().isDir())
						dirlist.add(filename);
				}
			} catch (Exception e) {
				MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색에 실패했습니다");
				MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "디렉토리 탐색에 실패했습니다");
				MsgBroadcaster.broadcastMsg(MsgListener.DIRTREE_FAIL, null);
				return;
			}
		}
		
		// Sort directory name
		Collections.sort(dirlist, String.CASE_INSENSITIVE_ORDER);
		
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
		MsgBroadcaster.broadcastMsg(MsgListener.DIRTREE_DONE, dirlist);
	}
	

	public static Vector<String> getDirectoryListSeq(String path) {
		// Check connection
		if (!SftpAdapter.isConnected())
			return new Vector<String>();
		
		ChannelSftp channel = SftpAdapter.getMainChannel();
		
		// Get directory list from sftp		
		Vector<String> dirlist = new Vector<String>();
		synchronized(channel) {
			try {
				Vector<?> lslist = channel.ls(path);
				Iterator<?> lsiter = lslist.iterator();
				while (lsiter.hasNext()) {
					LsEntry lsentry = (LsEntry)lsiter.next();
					// Add only directories. Skip hidden files
					String filename = ((LsEntry)lsentry).getFilename();
					if ((filename.charAt(0) != '.') && !filename.equals("recycle_bin") && ((LsEntry)lsentry).getAttrs().isDir())
						dirlist.add(filename);
				}
			} catch (Exception e) {
				return new Vector<String>();
			}
		}
		
		// Sort directory name
		Collections.sort(dirlist, String.CASE_INSENSITIVE_ORDER);
		
		return dirlist;
	}
	
	public static void getExploreFilesList(String path) {
		// Check connection
		if (!SftpAdapter.isConnected()) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.FILELIST_FAIL, null);
			return;
		}
		
		ChannelSftp channel = SftpAdapter.getMainChannel();
		
		// Get directory list from sftp
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일을 탐색중입니다");

		Vector<LsEntry> filelist = new Vector<LsEntry>();
		synchronized(channel) {
			try {
				Vector<?> lslist = channel.ls(path);
				Iterator<?> lsiter = lslist.iterator();
				while (lsiter.hasNext()) {
					LsEntry lsentry = (LsEntry)lsiter.next();
					// Add files and directories. Skip hidden files
					String filename = lsentry.getFilename();
					if ((filename.charAt(0) != '.') && !filename.equals("recycle_bin"))
						filelist.add(lsentry);
				}
			} catch (Exception e) {
				MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일 탐색에 실패했습니다");
				MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "파일 탐색에 실패했습니다");
				MsgBroadcaster.broadcastMsg(MsgListener.FILELIST_FAIL, null);
				return;
			}
		}
		
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일 탐색 완료");
		MsgBroadcaster.broadcastMsg(MsgListener.FILELIST_DONE, filelist);
	}
	
	public static void getSearchFilesList(String sword) {
		// Get search result from server
		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일을 검색중입니다");

		Vector<SearchEntry> filelist = new Vector<SearchEntry>();
		try {
			URL url = new URL("http://webhard.ksa14.org/search.php");
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			
			String param = URLEncoder.encode("search", "UTF-8") + "=" + URLEncoder.encode(sword, "UTF-8");
			
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(param);
			writer.flush();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			
			Gson gson = new Gson();
			SearchEntry[] results = gson.fromJson(reader, SearchEntry[].class);
			if (results != null) {
				for (SearchEntry res : results)
					filelist.add(res);
			}
		} catch (Exception e) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일 검색에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "파일 검색에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.SEARCH_FAIL, null);
			return;
		}

		MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "파일 검색 완료");
		MsgBroadcaster.broadcastMsg(MsgListener.SEARCH_DONE, filelist);
	}
	
	public static void createDirectory(String path, String dirname) {
		// Check connection
		if (!SftpAdapter.isConnected()) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.DIRTREE_FAIL, null);
			return;
		}

		try {
			ChannelSftp channel = SftpAdapter.getMainChannel();
			channel.mkdir(path + "/" + dirname);
		} catch (Exception e) {
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_INFO, "폴더 생성에 실패했습니다");
			MsgBroadcaster.broadcastMsg(MsgListener.STATUS_MESSAGE, "폴더 생성에 실패했습니다");
			return;
		}
		
		ExploreDirectoryTree.getInstance().updateTree();
		ExploreDirectoryTree.getInstance().addDirectory(dirname);
	}
}

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

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

public class SftpList {
	public static class SearchEntry {
		public String path = "";
		public String filename = "";
		public boolean isdir = false;
		public long filesize = 0;
		public int mtime = 0;
		
		public SearchEntry() {}
	}
	
	public static void GetDirectoryList(String path) {
		// Check connection
		if (!SftpAdapter.IsConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;
		}
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;	
		}
		
		// Get directory list from sftp
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리를 탐색중입니다");
		
		Vector<String> DirList = new Vector<String>();
		synchronized(channel) {
			try {
				Vector<?> ListV = channel.ls(path);
				Iterator<?> ListI = ListV.iterator();
				while (ListI.hasNext()) {
					Object CurObj = ListI.next();
					if (CurObj instanceof LsEntry) {
						// Add only directories
						String DirName = ((LsEntry)CurObj).getFilename();
						if ((DirName.charAt(0) != '.') && (DirName.compareTo("recycle_bin") != 0) && ((LsEntry)CurObj).getAttrs().isDir())
							DirList.add(DirName);
					}
				}
			} catch (SftpException e) {
				MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색에 실패했습니다");
				MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_FAIL, "디렉토리 탐색에 실패했습니다");
				return;
			}
		}
		
		// Sort directory name
		Collections.sort(DirList, String.CASE_INSENSITIVE_ORDER);
		
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_DONE, DirList);
	}

	public static Vector<String> GetDirectoryListNoMsg(String path) {
		
		// Check connection
		if (!SftpAdapter.IsConnected())
			return new Vector<String>();
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected())
			return new Vector<String>();
		
		// Get directory list from sftp
		Vector<String> DirList = new Vector<String>();
		synchronized(channel) {
			try {
				Vector<?> ListV = channel.ls(path);
				Iterator<?> ListI = ListV.iterator();
				while (ListI.hasNext()) {
					Object CurObj = ListI.next();
					if (CurObj instanceof LsEntry) {
						// Add only directories
						String DirName = ((LsEntry)CurObj).getFilename();
						if ((DirName.charAt(0) != '.') && (DirName.compareTo("recycle_bin") != 0) && ((LsEntry)CurObj).getAttrs().isDir())
							DirList.add(DirName);
					}
				}
			} catch (SftpException e) {
				return new Vector<String>();
			}
		}
		
		// Sort directory name
		Collections.sort(DirList, String.CASE_INSENSITIVE_ORDER);
		
		return DirList;
	}
	
	public static void GetExploreFilesList(String path) {
		// Check connection
		if (!SftpAdapter.IsConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;
		}
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;	
		}
		
		// Get file list from sftp
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일을 탐색중입니다");
		
		Vector<LsEntry> FileList = new Vector<LsEntry>();
		synchronized(channel) {
			try {
				Vector<?> ListV = channel.ls(path);
				Iterator<?> ListI = ListV.iterator();
				while (ListI.hasNext()) {
					Object CurObj = ListI.next();
					if (CurObj instanceof LsEntry) {
						String FileName = ((LsEntry)CurObj).getFilename();
						if ((FileName.charAt(0) != '.') && (FileName.compareTo("recycle_bin") != 0))
							FileList.add((LsEntry)CurObj);
					}
				}
			} catch (SftpException e) {
				MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 탐색에 실패했습니다");
				MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_FAIL, "파일 탐색에 실패했습니다");
				return;
			}
		}

		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 탐색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_DONE, FileList);
	}
	
	public static void GetSearchFilesList(String sword) {
		if (sword.trim().length() == 0) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "검색어를 입력해주세요");
			return;
		}
		
		// Check connection
		if (!SftpAdapter.IsConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;
		}
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.CONNECT_NONE, "서버에 접속되어 있지 않습니다");
			return;	
		}
		
		// Get search result from server 
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일을 검색중입니다");

		Vector<SearchEntry> FileList = new Vector<SearchEntry>();

		try {
			URL url = new URL("http://webhard.ksa14.org/search.php");
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			
			String param = URLEncoder.encode("search", "UTF-8") + "=" + URLEncoder.encode(sword, "UTF-8");
			
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
			osw.write(param);
			osw.flush();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			
			Gson gson = new Gson();
			SearchEntry[] results = gson.fromJson(reader, SearchEntry[].class);
			if (results != null) {
				for (SearchEntry res : results)
					FileList.add(res);
			}
		} catch (Exception e) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 검색에 실패했습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.SEARCH_FAIL, "파일 검색에 실패했습니다");
			return;
		}

		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 검색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.SEARCH_DONE, FileList);
	}
}

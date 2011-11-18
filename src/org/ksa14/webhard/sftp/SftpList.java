package org.ksa14.webhard.sftp;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

public class SftpList {
	public static class PathLsEntry {
		public String path;
		public LsEntry entry;
		
		public PathLsEntry(String p, LsEntry e) {
			path = p;
			entry = e;
		}
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
				DirList.clear();
			}
		}
		
		// Sort directory name
		Collections.sort(DirList, String.CASE_INSENSITIVE_ORDER);
		
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_DONE, DirList);
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
				FileList.clear();
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
		
		// Start search file from sftp
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일을 검색중입니다");

		Vector<PathLsEntry> FileList = new Vector<PathLsEntry>();
		synchronized(channel) {
			try {
				LinkedList<String> pathqueue = new LinkedList<String>();
				pathqueue.offer("/");
				do {
					String path = pathqueue.poll();
					Vector<?> ListV = channel.ls(path);
					Iterator<?> ListI = ListV.iterator();
					while (ListI.hasNext()) {
						Object CurObj = ListI.next();
						if (CurObj instanceof LsEntry) {
							LsEntry CurEntry = (LsEntry)CurObj;
							String FileName = CurEntry.getFilename();
							if ((FileName.charAt(0) != '.') && (FileName.compareTo("recycle_bin") != 0)) {
								if (CurEntry.getAttrs().isDir())
									pathqueue.offer(path + FileName + "/");
								if (FileName.toLowerCase().indexOf(sword.toLowerCase()) > -1)
									FileList.add(new PathLsEntry(path, CurEntry));
							}
						}
					}
				} while (!pathqueue.isEmpty());
			} catch (SftpException e) {
				MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 검색에 실패했습니다");
				MsgBroadcaster.BroadcastMsg(MsgListener.SEARCH_FAIL, "파일 검색에 실패했습니다");
				FileList.clear();
			}
		}

		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 검색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.SEARCH_DONE, FileList);
	}
}

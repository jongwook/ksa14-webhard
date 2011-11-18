package org.ksa14.webhard.sftp;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.ksa14.webhard.MsgBroadcaster;
import org.ksa14.webhard.MsgListener;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

public class SftpList {
	public static void GetDirectoryList(String path) {
		// Check connection
		if (!SftpAdapter.IsConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_FAIL, "서버에 접속되어 있지 않습니다");
			return;
		}
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_FAIL, "서버에 접속되어 있지 않습니다");
			return;			
		}
		
		// Get directory list from sftp
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리를 탐색중입니다");
		
		Vector<String> dirV = new Vector<String>();
		synchronized(channel) {
			try {
				Vector<?> lsV = channel.ls(path);
				Iterator<?> lsI = lsV.iterator();
				while (lsI.hasNext()) {
					Object curObj = lsI.next();
					if (curObj instanceof LsEntry) {
						String dirName = ((LsEntry)curObj).getFilename();
						if ((dirName.compareTo("recycle_bin") != 0) && ((LsEntry)curObj).getAttrs().isDir())
							dirV.add(dirName);
					}
				}
			} catch (SftpException e) {
				MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색에 실패했습니다");
				MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_FAIL, "디렉토리 탐색에 실패했습니다");
				dirV.clear();
			}
		}
		
		// Sort directory name
		Collections.sort(dirV, String.CASE_INSENSITIVE_ORDER);
		
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "디렉토리 탐색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.DIRTREE_DONE, dirV);
	}
	
	public static void GetFilesList(String path) {
		// Check connection
		if (!SftpAdapter.IsConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_FAIL, "서버에 접속되어 있지 않습니다");
			return;
		}
		ChannelSftp channel = SftpAdapter.getChannel("main");
		if (!channel.isConnected()) {
			MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "서버에 접속되어 있지 않습니다");
			MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_FAIL, "서버에 접속되어 있지 않습니다");
			return;			
		}
		
		// Get directory list from sftp
		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일을 탐색중입니다");
		
		Vector<LsEntry> fileV = new Vector<LsEntry>();
		synchronized(channel) {
			try {
				Vector<?> lsV = channel.ls(path);
				Iterator<?> lsI = lsV.iterator();
				while (lsI.hasNext()) {
					Object curObj = lsI.next();
					if (curObj instanceof LsEntry) {
						String fileName = ((LsEntry)curObj).getFilename();
						if (fileName.compareTo("recycle_bin") != 0)
							fileV.add((LsEntry)curObj);
					}					
				}
			} catch (SftpException e) {
				MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 탐색에 실패했습니다");
				MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_FAIL, "파일 탐색에 실패했습니다");
				fileV.clear();
			}
		}

		MsgBroadcaster.BroadcastMsg(MsgListener.STATUS_INFO, "파일 탐색 완료");
		MsgBroadcaster.BroadcastMsg(MsgListener.FILELIST_DONE, fileV);
	}
}

package org.ksa14.webhard.sftp;

import java.util.HashSet;

public class SftpPublisher {
	private static HashSet<SftpListener> listeners = new HashSet<SftpListener>();

	public static void AddListener(SftpListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}
	
	public static void RemoveListener(SftpListener listener) {
		synchronized(listeners) {
			listeners.remove(listeners);
		}
	}
	
	public static void RemoveAllListeners() {
		synchronized(listeners) {
			listeners.clear();
		}
	}
	
	public static void UpdateStatus(int type, Object arg) {
		synchronized(listeners) {
			for(SftpListener listener : listeners) {
				listener.UpdateStatus(type, arg);
			}
		}
	}
	
}

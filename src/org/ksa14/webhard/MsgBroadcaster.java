package org.ksa14.webhard;

import java.util.HashSet;

public class MsgBroadcaster {
	private static HashSet<MsgListener> listeners = new HashSet<MsgListener>();

	public static void AddListener(MsgListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public static void RemoveListener(MsgListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public static void ClearListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	public static void BroadcastMsg(int type, Object arg) {
		synchronized (listeners) {
			for (MsgListener listener : listeners)
				listener.ReceiveMsg(type, arg);
		}
	}
}

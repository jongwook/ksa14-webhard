package org.ksa14.webhard;

import java.util.HashSet;

public class MsgBroadcaster {
	private static HashSet<MsgListener> Listeners = new HashSet<MsgListener>();

	public static void AddListener(MsgListener listener) {
		synchronized (Listeners) {
			Listeners.add(listener);
		}
	}
	
	public static void RemoveListener(MsgListener listener) {
		synchronized (Listeners) {
			Listeners.remove(listener);
		}
	}
	
	public static void ClearListeners() {
		synchronized (Listeners) {
			Listeners.clear();
		}
	}
	
	public static void BroadcastMsg(int type, Object arg) {
		synchronized (Listeners) {
			for (MsgListener lsnr : Listeners)
				lsnr.ReceiveMsg(type, arg);
		}
	}
}

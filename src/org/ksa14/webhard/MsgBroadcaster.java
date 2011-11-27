package org.ksa14.webhard;

import java.util.HashSet;

/**
 * Broadcast messages to other classes
 * 
 * @author Jongwook, ThomasJun
 */
public class MsgBroadcaster {
	private static HashSet<MsgListener> listeners = new HashSet<MsgListener>();

	public static void addListener(MsgListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public static void removeListener(MsgListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public static void clearlisteners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	public static void broadcastMsg(int type, Object arg) {
		synchronized (listeners) {
			for (MsgListener listener : listeners)
				listener.receiveMsg(type, arg);
		}
	}
}

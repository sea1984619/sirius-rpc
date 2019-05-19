package org.sirius.transport.api;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 管理当前地址的链接在断开时,是否需要重连
 */
public class AddressReconnectManager {
	
	private static ConcurrentHashMap<UnresolvedAddress , Boolean> map = new ConcurrentHashMap<UnresolvedAddress , Boolean>();
	
	public static void setReconnect(UnresolvedAddress address,Boolean needed) {
		map.put(address, needed);
	}
	
	public static void cancelReconnect(UnresolvedAddress address) {
		map.put(address, false);
	}

	public static boolean isNeedReonnent(UnresolvedAddress address) {
		Boolean isNeeded  = map.get(address);
		return isNeeded == null ? true : isNeeded;
	}
	
	public static void cancelAllReconnect() {
		for(UnresolvedAddress address : map.keySet()) {
			cancelReconnect(address);
		}
	}
}

package org.sirius.transport.api.channel;

import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;

public class GroupListDirectory {

/*
 * 服务标识到channellist的映射
 */
private ConcurrentMap<String,ChannelGroupList>  map = Maps.newConcurrentMap();
	
	public ChannelGroupList getGroupList(String  serviceId) {
		ChannelGroupList list = map.get(serviceId);
		if(list == null) {
			list = new  ChannelGroupList();
			map.put(serviceId, list);
			list = map.get(serviceId);
		}
		return list;
	}
	
	public void addGroupList(String serviceID,ChannelGroupList list ) {
		map.put(serviceID,list);
	}
	
	public void removeGroupList(String serviceID) {
		map.remove(serviceID);
	}
}

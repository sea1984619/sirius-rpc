package org.sirius.transport.api.channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.transport.api.UnresolvedAddress;

public class GroupListDirectory {

	/* key:远程服务标识*/
private ConcurrentMap<String,ChannelGroupList>  serviceMap = Maps.newConcurrentMap();

    public ChannelGroupList getGroupList(String  serviceId) {
		ChannelGroupList list = serviceMap.get(serviceId);
		if(list == null) {
			list = new  ChannelGroupList();
			serviceMap.put(serviceId, list);
			list = serviceMap.get(serviceId);
		}
		return list;
	}
	
	public void removeChannelGroup(UnresolvedAddress address) {
		for(Entry<String, ChannelGroupList> groupList: serviceMap.entrySet()) {
			groupList.getValue().remove(address);
		}
	}
	public void addGroupList(String serviceID,ChannelGroupList list ) {
		serviceMap.put(serviceID,list);
	}
	
	public void removeGroupList(String serviceID) {
		serviceMap.remove(serviceID);
	}
}

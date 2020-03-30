package org.sirius.transport.api.channel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sirius.transport.api.UnresolvedAddress;

/*
 * 服务相同, 但地址不同的channelGroup
 */
public class ChannelGroupList {

	private CopyOnWriteArrayList<ChannelGroup> groupList = new CopyOnWriteArrayList<ChannelGroup>();

	public List<ChannelGroup> getList(){
		return this.groupList;
	}
	public boolean add(ChannelGroup group) {
		boolean added = group != null && groupList.addIfAbsent(group);
		return added;
	}

	public boolean remove(ChannelGroup group) {
		return groupList.remove(group);
	}
	
	public void remove(UnresolvedAddress address) {
		for(ChannelGroup group: groupList) {
			if(group.remoteAddress().equals(address))
				groupList.remove(group);
		}
	}

	public int size() {
		return groupList.size();
	}
}

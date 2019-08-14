package org.sirius.transport.api.channel;

import java.util.concurrent.CopyOnWriteArrayList;

/*
 * 服务相同, 但地址不同的channelGroup
 */
public class ChannelGroupList {

	private CopyOnWriteArrayList<ChannelGroup> groupList = new CopyOnWriteArrayList<ChannelGroup>();

	public boolean add(ChannelGroup group) {
		boolean added = group != null && groupList.addIfAbsent(group);
		return added;
	}

	public boolean remove(ChannelGroup group) {

		return groupList.remove(group);
	}

	public int size() {
		return groupList.size();
	}
}

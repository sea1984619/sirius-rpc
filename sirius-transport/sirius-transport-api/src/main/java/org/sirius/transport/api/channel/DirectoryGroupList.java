package org.sirius.transport.api.channel;

import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.transport.api.Directory;

/*
 * 服务到地址的映射
 */
public class DirectoryGroupList {
	private ConcurrentMap<String,ChannelGroupList>  map = Maps.newConcurrentMap();
	
	public ChannelGroupList getGroupList(Directory directory) {
		return map.get(directory.directoryString());
	}
	
	public void addGroupList(Directory directory,ChannelGroupList list ) {
		map.put(directory.directoryString(),list);
	}
}

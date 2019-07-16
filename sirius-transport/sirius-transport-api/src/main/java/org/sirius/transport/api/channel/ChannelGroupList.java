package org.sirius.transport.api.channel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 服务相同, 但地址不同的channelGroup
 */
public class ChannelGroupList {
	//简单的版本控制 ,表示当前list有所改变 , 在负载均衡时 需要重新计算权重数组
	private volatile AtomicInteger vision = new AtomicInteger(0);
	CopyOnWriteArrayList<ChannelGroup> groupList = new CopyOnWriteArrayList<ChannelGroup>();
	
	public int getVesion() {
		return this.vision.get();
	}
	public boolean add(ChannelGroup group) {
		boolean  added = group != null && groupList.addIfAbsent(group);
		if(added)
		  vision.incrementAndGet();
		return added;
	}
	
   public boolean remove(ChannelGroup group) {
	   boolean removed = groupList.remove(group);
	   if(removed)
	     vision.incrementAndGet();
	   return removed;
	   
   }
   
   public List<ChannelGroup> getChannelGroup(){
	   return this.groupList;
   }
}

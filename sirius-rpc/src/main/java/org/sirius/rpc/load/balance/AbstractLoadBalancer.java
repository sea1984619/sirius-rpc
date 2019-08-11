package org.sirius.rpc.load.balance;

import java.util.List;

import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.ChannelGroupList.Warper;

public abstract class AbstractLoadBalancer implements LoadBalancer{

	public ChannelGroup select(ChannelGroupList list) {
		Warper warper = list.getWarper();
		List<ChannelGroup> groups = warper.getGroupList();
		if(groups.size() == 0) {
			throw new IllegalStateException("this is No available channelGroupList ");
		}
		if(groups.size() == 1) {
			return groups.get(0);
		}
		int[] weights = warper.getWeights();
		return doSelect(groups,weights);
	}


	public abstract ChannelGroup doSelect(List<ChannelGroup> groups, int[] weights) ;

} 

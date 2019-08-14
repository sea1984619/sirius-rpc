package org.sirius.rpc.consumer.loadbalance;

import java.util.List;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;

public abstract class AbstractLoadBalancer implements LoadBalancer {

	public ChannelGroup select(List<ChannelGroup> list, Request request) {

		if (list.size() == 0) {
			throw new IllegalStateException("this is No available channelGroup ");
		}
		if (list.size() == 1) {
			return list.get(0);
		}

		return doSelect(list, request);
	}

	public abstract ChannelGroup doSelect(List<ChannelGroup> groups, Request request);

}

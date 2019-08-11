package org.sirius.rpc.load.balance;

import java.util.List;

import org.sirius.common.util.IntegerSequencer;
import org.sirius.transport.api.channel.ChannelGroup;

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

	@Override
	public ChannelGroup doSelect(List<ChannelGroup> groups, int[] weights) {
		// TODO Auto-generated method stub
		return null;
	}

}

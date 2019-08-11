package org.sirius.rpc.consumer.load.balance;


import org.sirius.common.ext.Extensible;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.channel.ChannelGroupList;


@Extensible
public interface LoadBalancer{

	ChannelGroup select(ChannelGroupList list);
}

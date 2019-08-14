package org.sirius.rpc.consumer.loadbalance;


import java.util.List;

import org.sirius.common.ext.Extensible;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;


@Extensible
public interface LoadBalancer{

	ChannelGroup select(List<ChannelGroup> list ,Request request);
}

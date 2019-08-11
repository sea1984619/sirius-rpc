package org.sirius.rpc.consumer.router;

import java.util.List;

import org.sirius.common.ext.Extensible;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;

@Extensible
public interface Router {

	List<ChannelGroup> route(List<ChannelGroup> groupList, Request request);
}

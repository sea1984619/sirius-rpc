package org.sirius.rpc.consumer;

import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public interface ConsumerProcessor {

	void handleResponse(Channel channel,Response response);
	
	void shutdown();

}

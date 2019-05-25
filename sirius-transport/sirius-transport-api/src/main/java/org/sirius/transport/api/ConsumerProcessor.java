package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;

public interface ConsumerProcessor {

	void handleResponse(Channel channel,Response response);
	
	void shutdown();

}

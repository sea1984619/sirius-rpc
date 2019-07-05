package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;

public interface ProviderProcessor {

	void handlerRequest(Channel channel, Request request);
	
	void handlerException(Channel channel, Request request, Throwable e);	
	
	void shutdown();

	
}

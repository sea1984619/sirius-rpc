package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;

public interface ProviderProcessor {

	void handlerRequest(Channel channel, Request request);
	
	void handlerException(Channel channel, Throwable e);
	
	void shutdown();
}

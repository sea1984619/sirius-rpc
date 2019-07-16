package org.sirius.transport.api.channel;

public interface ChannelListener {

	void onClosed(Channel channel);
	
	void onConnected(Channel channel);
}

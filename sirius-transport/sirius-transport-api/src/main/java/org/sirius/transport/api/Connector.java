package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;

public interface Connector {

Config getConfig();
	
	void setConfig(Config config);
	
	ConsumerProcessor consumerProcessor();
	
	void setConsumerProcessor(ConsumerProcessor c);
	
	Channel connect(UnresolvedAddress address);
	
	Channel connect(UnresolvedAddress address,boolean async);
	
	ChannelGroup group(UnresolvedAddress address);
	
	void shutdownGracefully();
}

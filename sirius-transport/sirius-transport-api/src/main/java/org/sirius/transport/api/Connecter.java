package org.sirius.transport.api;

import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.rpc.consumer.ConsumerProcessor;

public interface Connecter extends Transporter {

	Config getConfig();
	
	ConsumerProcessor consumerProcessor();
	
	void setConsumerProcessor(ConsumerProcessor c);
	
	Connection connect(UnresolvedAddress address);
	
	Connection connect(UnresolvedAddress address,boolean async);
	
	ChannelGroup group(UnresolvedAddress address);
	
	void shutdownGracefully();
}

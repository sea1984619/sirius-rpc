package org.sirius.rpc.client;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.channel.ChannelGroupList;

public interface  RpcClient {

	 Connector getConnector();
	 
	 ConsumerProcessor getProcessor();
	 
	 ChannelGroupList getGroupList(String serviceID);
	 
	 void addConsumerConfig(ConsumerConfig<?> consumerConfig);
	 
     void Start();
	
	 void Shutdown();
	
}

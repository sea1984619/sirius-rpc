package org.sirius.rpc;

import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;

public interface  RpcClient {

	 Connector getConnector();
	 
	 ConsumerProcessor getProcessor();
	 
     void Start();
	
	 void Shutdown();
	
}

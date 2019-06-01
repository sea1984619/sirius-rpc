package org.sirius.rpc;

import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.netty.NettyTcpConnector;

public class DefaultRpcClient implements RpcClient {
	
	private Connector connector;
	private ConsumerProcessor processor;
	
	public DefaultRpcClient(Connector connector, ConsumerProcessor processor) {
		this.connector = (NettyTcpConnector) connector;
		this.processor =  (DefaultConsumerProcessor) processor;
		this.connector.setConsumerProcessor(processor);
	}

	@Override
	public void Start() {
		UnresolvedAddress  address = new UnresolvedSocketAddress("127.0.0.7",18090);
		connector.connect(address);
	}

	@Override
	public Connector getConnector() {
		return this.connector;
	}
	

	@Override
	public ConsumerProcessor getProcessor() {
		return this.processor;
		}

	@Override
	public void Shutdown() {
		
	}
	
}

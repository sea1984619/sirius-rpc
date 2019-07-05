package org.sirius.rpc.client;

import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.load.balance.LoadBalancer;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;

public class DefaultRpcClient implements RpcClient {
	
	private ConcurrentMap<Class<?>,ConsumerConfig> configs = Maps.newConcurrentMap();
	private ConcurrentMap<Class<?>,Invoker>  invokers = Maps.newConcurrentMap();
	private Registry registry ;
	private LoadBalancer balancer;
	
	private Connector connector;
	private ConsumerProcessor processor;
	
	public DefaultRpcClient(Connector connector, ConsumerProcessor processor) {
		this.connector = connector;
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

package org.sirius.rpc.client;

import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.DirectoryGroupList;
import org.sirius.transport.netty.NettyTcpConnector;

public class DefaultRpcClient implements RpcClient {
	
	private volatile static RpcClient client;
	private ConcurrentMap<Class<?>,ConsumerConfig> configs = Maps.newConcurrentMap();
	private ConcurrentMap<Class<?>,Invoker>  invokers = Maps.newConcurrentMap();
	private List<Registry> registrys;
	private DirectoryGroupList directory;
	private Connector connector;
	private ConsumerProcessor processor;
	private ConcurrentMap<String,ChannelGroupList> groupList = Maps.newConcurrentMap();
	private DefaultRpcClient() {
		init();
	}
	
	private void init() {
		connector = new NettyTcpConnector();
		processor = new DefaultConsumerProcessor();
		connector.setConsumerProcessor(processor);
	}

	public static RpcClient getInstance() {
		if(client == null) {
			synchronized(DefaultRpcClient.class) {
				if(client == null) {
					client = new DefaultRpcClient();
				}
			}
		}
		return client;
		
	}

	@Override
	public void Start() {
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

	@Override
	public ChannelGroupList getGroupList(String serviceID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addConsumerConfig(ConsumerConfig<?> consumerConfig) {
		// TODO Auto-generated method stub
		
	}
	
}

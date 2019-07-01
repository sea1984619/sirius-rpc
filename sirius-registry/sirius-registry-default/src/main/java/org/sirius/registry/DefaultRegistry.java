package org.sirius.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.Registry;
import org.sirius.rpc.registry.RegistryServer;


public class DefaultRegistry implements Registry{
	
	private  RegistryServer server;
	private  ConcurrentHashMap  listeners =  (ConcurrentHashMap) Maps.newConcurrentMap();
	        
	@Override
	public void register(ProviderConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Unregister(ProviderConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unSubscribe(ConsumerConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}

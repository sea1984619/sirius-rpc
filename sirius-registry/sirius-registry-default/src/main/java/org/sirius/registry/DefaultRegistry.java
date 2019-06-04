package org.sirius.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.sirius.registry.api.ProviderGroup;
import org.sirius.registry.api.ProviderInfo;
import org.sirius.registry.api.Registry;
import org.sirius.registry.api.RegistryServer;

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

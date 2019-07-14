package org.sirius.rpc.registry;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;

public  class AbstractRegistry implements Registry{

	@Override
	public void register(ProviderConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Unregister(ProviderConfig config) {
		// TODO Auto-generated method stub
		
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

	@Override
	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig config, ProviderInfoListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

}

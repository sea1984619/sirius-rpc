package org.sirius.registry.zookeeper;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.registry.AbstractRegistry;
import org.sirius.rpc.registry.ProviderInfoListener;

public class zookeeperRegistry extends AbstractRegistry{

	public zookeeperRegistry(RegistryConfig config) {
		super(config);
	}

	@Override
	protected void init() {
		
	}

	@Override
	protected void doRegister(ProviderConfig config) {
		
	}

	@Override
	protected void doUnSubscribe(ConsumerConfig config) {
		
	}

	@Override
	protected void doUnregister(ProviderConfig config) {
		
	}

	@Override
	protected void doSubscribe(ConsumerConfig config, ProviderInfoListener listener) {
		
	}

}

package org.sirius.rpc.registry;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;


public interface RegistryService {

	public void register(ProviderConfig config);

	public void Unregister(ProviderConfig config);

	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig config);

	public void unSubscribe(ConsumerConfig config);
}

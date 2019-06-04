package org.sirius.registry.api;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;

public interface RegistryService {

	public void register(ProviderConfig config);

	public void Unregister(ProviderConfig config);

	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig config);

	public void unSubscribe(ConsumerConfig config);
}

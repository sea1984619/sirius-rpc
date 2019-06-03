package org.sirius.registry.api;

import java.util.List;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;

public interface RegistryService {

	public void register(ProviderConfig config);

	public void Unregister(ProviderConfig config);

	public List<ProviderGroup> subscribe(ConsumerConfig config);

	public void unSubscribe(ConsumerConfig config);
}

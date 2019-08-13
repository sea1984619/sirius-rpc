package org.sirius.rpc.registry;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;


public interface RegistryService {

	public void register(ProviderConfig<?> config);

	public void unRegister(ProviderConfig<?> config);

	public void subscribe(ConsumerConfig<?> config ,NotifyListener listener);

	public void unSubscribe(ConsumerConfig<?> config);
}

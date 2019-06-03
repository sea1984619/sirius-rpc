package org.sirius.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.sirius.registry.api.ProviderGroup;
import org.sirius.registry.api.RegistryService;

@SuppressWarnings("rawtypes")
public class DefaultRegistryService  implements RegistryService{

	ConcurrentHashMap  listeners =  (ConcurrentHashMap) Maps.newConcurrentMap();
	ConcurrentHashSet<ProviderConfig>  providers =  new ConcurrentHashSet<ProviderConfig>();
	ConcurrentHashSet<ConsumerConfig>  consumers =  new ConcurrentHashSet<ConsumerConfig>();
	ConcurrentHashMap<String,List<ProviderGroup>>  providerGroupMap =  (ConcurrentHashMap) Maps.newConcurrentMap();
	
	@Override
	public void register(ProviderConfig provider) {
		providers.add(provider);
	}

	@Override
	public void Unregister(ProviderConfig provider) {
		providers.remove(provider);
	}

	@Override
	public List<ProviderGroup> subscribe(ConsumerConfig consumer) {
		consumers.add(consumer);
		return null;
	}

	@Override
	public void unSubscribe(ConsumerConfig consumer) {
		consumers.remove(consumer);
	}

}

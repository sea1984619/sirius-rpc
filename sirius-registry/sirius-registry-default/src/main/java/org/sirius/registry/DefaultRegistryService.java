package org.sirius.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.RegistryService;

@SuppressWarnings("rawtypes")
public class DefaultRegistryService implements RegistryService {

	// 所有的订阅者 ,  key ->服务标识, value -> 所有订阅者ip集合,不包括port
	ConcurrentMap<String, ConcurrentHashSet<String>> consumers = Maps.newConcurrentMap();
	// 所有的发布者,  key ->服务标识, value -> 所有发布者ip集合,不包括port
	ConcurrentMap<String, ConcurrentHashSet<String>> providers = new ConcurrentHashMap();
	// 发布者发布的可用信息 key-> 发布者ip,不包括port
	ConcurrentMap<String, ConcurrentHashSet<ProviderInfo>> providerInfoMap = Maps.newConcurrentMap();
	// 某一服务对应的所有可用信息  key-> 服务标识
	ConcurrentMap<String, ConcurrentHashSet<ProviderInfo>> AllProviderInfoMap = Maps.newConcurrentMap();

	DefaultRegistryServer server ;
	public DefaultRegistryService(DefaultRegistryServer defaultRegistryServer) {
		this.server =  defaultRegistryServer;
	}

	@Override
	public void register(ProviderConfig provider) {
		String uniqueId = provider.getUniqueId();
		List<ProviderInfo> infoList = getInfoFromConfig(provider);
		ConcurrentHashSet<ProviderInfo> providerInfoSet =  providerInfoMap.get(uniqueId);
		if (providerInfoSet == null) {
			providerInfoSet = new ConcurrentHashSet<ProviderInfo>();
			providerInfoMap.putIfAbsent(uniqueId, providerInfoSet);
			providerInfoSet = providerInfoMap.get(uniqueId);
		}
		
		providerInfoSet.addAll(infoList);
		
		ConcurrentHashSet<ProviderInfo> AllProviderInfoSet = AllProviderInfoMap.get(uniqueId);
		if (AllProviderInfoSet == null) {
			AllProviderInfoSet = new ConcurrentHashSet<ProviderInfo>();
			AllProviderInfoMap.putIfAbsent(uniqueId, providerInfoSet);
			AllProviderInfoSet = providerInfoMap.get(uniqueId);
		}
		AllProviderInfoSet.addAll(infoList);
	}

	@Override
	public void Unregister(ProviderConfig provider) {
	}

	@Override
	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig consumer) {
		String uniqueId = consumer.getUniqueId();
		ConcurrentHashSet<ProviderInfo> set  = AllProviderInfoMap.get(uniqueId);
		return set;
	}

	@Override
	public void unSubscribe(ConsumerConfig consumer) {

	}

	private List<ProviderInfo> getInfoFromConfig(ProviderConfig<?> provider) {

		List<ServerConfig> configList = provider.getServerRef();
		List<ProviderInfo> infoList = new ArrayList<ProviderInfo>();
//		for (ServerConfig server : configList) {
//			ProviderInfo info = new ProviderInfo();
//			info.setHost(server.getHost());
//			info.setPort(server.getPort());
//			info.setWeight(provider.getWeight());
//			infoList.add(info);
//		}
		ProviderInfo info = new ProviderInfo();
		info.setHost("127.0.0.1");
		info.setPort(10890);
		infoList.add(info);
		return infoList;
	}
	

	public static void main(String args[]) throws InterruptedException, ExecutionException {
	}
}

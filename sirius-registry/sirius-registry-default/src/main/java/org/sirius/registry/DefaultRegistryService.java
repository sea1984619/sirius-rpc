package org.sirius.registry;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.sirius.config.ServerConfig;
import org.sirius.registry.api.ProviderGroup;
import org.sirius.registry.api.ProviderInfo;
import org.sirius.registry.api.RegistryService;
import org.sirius.transport.api.channel.Channel;

@SuppressWarnings("rawtypes")
public class DefaultRegistryService implements RegistryService {

	// 所有的订阅者,用实际地址表示
	ConcurrentMap<String, ConcurrentHashSet<SocketAddress>> consumers = Maps.newConcurrentMap();
	// 所有的发布者,用实际地址表示
	ConcurrentMap<String, ConcurrentHashSet<SocketAddress>> providers = Maps.newConcurrentMap();
	// 订阅者持有的链接
	ConcurrentMap<SocketAddress, List<Channel>> consumerChannels = Maps.newConcurrentMap();
	// 发布者持有的链接
	ConcurrentMap<SocketAddress, List<Channel>> providerChannels = Maps.newConcurrentMap();
	// 发布者发布的可用信息
	ConcurrentMap<ProviderConfig, ConcurrentHashSet<ProviderInfo>> providerToInfoMap = Maps.newConcurrentMap();
	// 某一服务对应的所有可用信息
	ConcurrentMap<String, List<ProviderGroup>> providerGroupMap = Maps.newConcurrentMap();

	@Override
	public void register(ProviderConfig provider) {
		String uniqueId = provider.getUniqueId();
		List<ProviderGroup> providerGroup = providerGroupMap.get(uniqueId);
		if (providerGroup == null) {
			providerGroup = new ArrayList<ProviderGroup>();
			providerGroupMap.putIfAbsent(uniqueId, providerGroup);
			providerGroup = providerGroupMap.get(uniqueId);
		}
		List<ProviderInfo> infoList = getInfoFromConfig(provider);
	}

	@Override
	public void Unregister(ProviderConfig provider) {
	}

	@Override
	public List<ProviderGroup> subscribe(ConsumerConfig consumer) {
		return null;
	}

	@Override
	public void unSubscribe(ConsumerConfig consumer) {

	}

	private List<ProviderInfo> getInfoFromConfig(ProviderConfig<?> provider) {

		List<ServerConfig> configList = provider.getServer();
		List<ProviderInfo> infoList = new ArrayList<ProviderInfo>();
		for (ServerConfig server : configList) {
			ProviderInfo info = new ProviderInfo();
			info.setHost(server.getHost());
			info.setPort(server.getPort());
			info.setWeight(provider.getWeight());
			infoList.add(info);
		}
		return infoList;
	}
}

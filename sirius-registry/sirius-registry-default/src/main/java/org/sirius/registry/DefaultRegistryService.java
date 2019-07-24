package org.sirius.registry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
import org.sirius.common.util.NetUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.ProviderInfoGroup;
import org.sirius.rpc.registry.ProviderInfoListener;
import org.sirius.rpc.registry.RegistryService;
import org.sirius.transport.api.channel.Channel;

@SuppressWarnings("rawtypes")
public class DefaultRegistryService implements RegistryService {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryService.class);
	private static String CHANNEL = "channel";
	// key ->服务标识 : value -> 订阅者监听器集合
	ConcurrentMap<String, ConcurrentHashSet<ProviderInfoListener>> consumerListerners = Maps.newConcurrentMap();
	// key ->ip+host : value -> 发布的信息
	// 一个IP地址下可能发布有不同的服务,而同一个服务可能会在不同端口上发布
	ConcurrentMap<String, ConcurrentMap<String, ProviderInfoGroup>> providers = Maps.newConcurrentMap();
	// key ->服务标识 : value -> 所有可用的信息集合
	ConcurrentMap<String, ProviderInfoGroup> providerInfoGroup = Maps.newConcurrentMap();

	public DefaultRegistryService() {
	}

	@Override
	public void register(ProviderConfig provider) {
		String uniqueId = provider.getInterface();
		List<ProviderInfo> temList = getInfoFromConfig(provider);

		InetSocketAddress address = getRemoteAddress();
		String address_key = address.getHostString() + address.getPort();
		ConcurrentMap<String, ProviderInfoGroup> groups = providers.get(address_key);

		if (groups == null) {
			providers.putIfAbsent(address_key, Maps.newConcurrentMap());
			groups = providers.get(address_key);
		}
		ProviderInfoGroup group = groups.get(uniqueId);
		if (group == null) {
			groups.putIfAbsent(uniqueId, new ProviderInfoGroup(uniqueId));
			group = groups.get(uniqueId);
		}

		group.addAll(temList);

		ProviderInfoGroup allGroup = providerInfoGroup.get(uniqueId);
		if (allGroup == null) {
			providerInfoGroup.putIfAbsent(uniqueId, new ProviderInfoGroup(uniqueId));
			allGroup = providerInfoGroup.get(uniqueId);
		}
		allGroup.addAll(temList);
		
		ConcurrentHashSet<ProviderInfoListener> listeners = consumerListerners.get(uniqueId);
		if (listeners != null) {
			for (ProviderInfoListener listener : listeners) {
				try {
					listener.notifyOnLine(group);
				} catch (Throwable t) {
					logger.error("notifyOnLine failed..", t);
					continue;
				}
			}
		}
	}

	@Override
	public void subscribe(ConsumerConfig config, ProviderInfoListener listener) {
		String key = config.getInterface();
		ConcurrentHashSet<ProviderInfoListener> listenerSet = consumerListerners.get(key);
		if (listenerSet == null) {
			consumerListerners.putIfAbsent(key, new ConcurrentHashSet<ProviderInfoListener>());
			listenerSet = consumerListerners.get(key);
		}
		listenerSet.add(listener);
		ProviderInfoGroup group = providerInfoGroup.get(key);
		if (group != null && !group.getProviderInfos().isEmpty()) {
			listener.notifyOnLine(group);
		}
	}

	@Override
	public void unSubscribe(ConsumerConfig consumer) {

	}

	@Override
	public void unRegister(ProviderConfig config) {

	}

	private List<ProviderInfo> getInfoFromConfig(ProviderConfig<?> provider) {
		List<ServerConfig> configList = provider.getServerRef();
		List<ProviderInfo> infoList = new ArrayList<ProviderInfo>();
		for (ServerConfig server : configList) {
			ProviderInfo info = new ProviderInfo();
			String host = server.getHost();
			if (NetUtils.isInvalidLocalHost(host)) {
				host = getRemoteAddress().getHostString();
			}
			info.setHost(host);
			info.setPort(server.getPort());
			info.setWeight(provider.getWeight());
			infoList.add(info);
		}
		return infoList;
	}

	private InetSocketAddress getRemoteAddress() {
		Channel channel = (Channel) RpcInvokeContent.getContent().get(CHANNEL);
		return (InetSocketAddress) channel.remoteAddress();
	}
}

package org.sirius.registry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;	

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
import org.sirius.rpc.registry.RegistryService;
import org.sirius.transport.api.channel.Channel;
import org.sirius.rpc.registry.NotifyListener;

@SuppressWarnings("rawtypes")
public class DefaultRegistryService implements RegistryService {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryService.class);
	private static String CHANNEL = "channel";
	// key ->服务标识 : value -> 订阅者监听器集合
	ConcurrentMap<String, ConcurrentHashSet<NotifyListener>> consumerListerners = Maps.newConcurrentMap();
	// key ->ip+host : value -> 发布的信息
	// 一个IP地址下可能发布有不同的服务,而同一个服务可能会在不同端口上发布
	ConcurrentMap<String, ConcurrentMap<String, ProviderGroup>> providers = Maps.newConcurrentMap();
	// key ->服务标识 : value -> 所有可用的信息集合
	ConcurrentMap<String, ProviderGroup> allProviders = Maps.newConcurrentMap();

	public DefaultRegistryService() {
	}

	@Override
	public void register(ProviderConfig provider) {
		String uniqueId = provider.getInterface();
		List<ProviderInfo> temList = getInfoFromConfig(provider);
		
		ConcurrentHashSet<NotifyListener> listeners = consumerListerners.get(uniqueId);
		if (listeners != null) {
			for (NotifyListener listener : listeners) {
				try {
					for (ProviderInfo info : temList) {
						listener.providerOffLine(info);
					}
				} catch (Throwable t) {
					logger.error("notifyOnLine failed..", t);
					continue;
				}
			}
		}

		InetSocketAddress address = getRemoteAddress();
		String address_key = address.getHostString() + address.getPort();
		ConcurrentMap<String, ProviderGroup> groups = providers.get(address_key);

		if (groups == null) {
			providers.putIfAbsent(address_key, Maps.newConcurrentMap());
			groups = providers.get(address_key);
		}
		ProviderGroup group = groups.get(uniqueId);
		if (group == null) {
			groups.putIfAbsent(uniqueId, new ProviderGroup(uniqueId));
			group = groups.get(uniqueId);
		}

		group.addAll(temList);

		ProviderGroup allGroup = allProviders.get(uniqueId);
		if (allGroup == null) {
			allProviders.putIfAbsent(uniqueId, new ProviderGroup(uniqueId));
			allGroup = allProviders.get(uniqueId);
		}
		allGroup.addAll(temList);

	}

	@Override
	public void subscribe(ConsumerConfig config, NotifyListener listener) {
		String key = config.getInterface();
		ConcurrentHashSet<NotifyListener> listenerSet = consumerListerners.get(key);
		if (listenerSet == null) {
			consumerListerners.putIfAbsent(key, new ConcurrentHashSet<NotifyListener>());
			listenerSet = consumerListerners.get(key);
		}
		listenerSet.add(listener);
		ProviderGroup group = allProviders.get(key);
		if (group != null) {
			try {
				for (ProviderInfo info : group.getProviderInfos()) {
					listener.providerOffLine(info);
				}
			} catch (Throwable t) {
				logger.error("notifyOnLine failed..", t);
			}
		}
	}

	@Override
	public void unSubscribe(ConsumerConfig consumer) {
		consumerListerners.remove(consumer.getInterface());
	}

	@Override
	public void unRegister(ProviderConfig config) {
		String uniqueId = config.getInterface();
		InetSocketAddress address = getRemoteAddress();
		String address_key = address.getHostString() + address.getPort();
		ConcurrentMap<String, ProviderGroup> groups = providers.get(address_key);
		ProviderGroup group = groups.remove(uniqueId);
		ProviderGroup allGroups = allProviders.get(uniqueId);
		allGroups.removeAll(group.getProviderInfos());

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

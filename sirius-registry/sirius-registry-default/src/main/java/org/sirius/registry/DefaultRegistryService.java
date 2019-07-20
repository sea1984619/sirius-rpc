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
	// key ->ip+host 确定一个唯一发布者 : value -> 发布的信息
	ConcurrentMap<String, ProviderInfoGroup> providers = Maps.newConcurrentMap();
	// key ->服务标识 : value -> 所有可用的信息集合
	ConcurrentMap<String, ProviderInfoGroup> providerInfoGroup = Maps.newConcurrentMap();

	public DefaultRegistryService() {
	}

	@Override
	public void register(ProviderConfig provider) {
		String uniqueId = provider.getInterface();
		List<ProviderInfo> temList = getInfoFromConfig(provider);
		
		InetSocketAddress address = getRemoteAddress();
		String key = address.getHostString() + address.getPort();
		ProviderInfoGroup group = providers.get(key);
		ConcurrentHashSet<ProviderInfoListener> listeners = consumerListerners.get(uniqueId);
		if (group == null) {
			//第一次注册;
			logger.info("register service {}.. " ,uniqueId);
			group = new ProviderInfoGroup(uniqueId);
			providers.putIfAbsent(key, group);
			group = providers.get(key);
			group.addAll(temList);
			if(listeners != null) {
				for(ProviderInfoListener listener : listeners) {
					try {
						listener.notifyOnLine(group);
					}catch(Throwable t) {
						logger.error("notifyOnLine failed.." ,t);
						continue;
					}
				}
			}
			
		}else {
			group.addAll(temList);
			if(listeners != null) {
				for(ProviderInfoListener listener : listeners) {
					listener.notifyUpdate(group);
				}
			}
		}
		
		ProviderInfoGroup allGroup = providerInfoGroup.get(uniqueId);
		if (allGroup == null) {
			allGroup = new ProviderInfoGroup(uniqueId);
			providerInfoGroup.putIfAbsent(uniqueId, allGroup);
			allGroup = providerInfoGroup.get(uniqueId);
		}
		allGroup.addAll(temList);
	}

	@Override
	public  void subscribe(ConsumerConfig config, ProviderInfoListener listener) {
		String key  = config.getUniqueId();
		ConcurrentHashSet<ProviderInfoListener> listenerSet = consumerListerners.get(key);
		if(listenerSet == null) {
			listenerSet = new ConcurrentHashSet<ProviderInfoListener>();
			consumerListerners.putIfAbsent(key, listenerSet);
			listenerSet = consumerListerners.get(key);
		}
		listenerSet.add(listener);
		ProviderInfoGroup group = providerInfoGroup.get(key);
		if(group != null) {
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

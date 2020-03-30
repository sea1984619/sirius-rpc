package org.sirius.rpc.client;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.common.util.ThrowUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.registry.NotifyListener;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.Registry;
import org.sirius.rpc.registry.RegistryFactory;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.GroupListDirectory;
import org.sirius.transport.netty.NettyTcpConnector;

/*
 * 负责与注册中心沟通,提供各种网络服务
 */
public class DefaultRpcClient implements RpcClient {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRpcClient.class);
	private volatile static RpcClient instance;
	private GroupListDirectory directory = new GroupListDirectory();
	private Connector connector;
	private ConsumerProcessor processor;
	private ConcurrentMap<String,ConsumerConfig<?>>  configMap = Maps.newConcurrentMap();

	private DefaultRpcClient() {
		init();
	}

	private void init() {
		connector = new NettyTcpConnector();
		processor = new DefaultConsumerProcessor();
		connector.setConsumerProcessor(processor);
	}

	public static RpcClient getInstance() {
		if (instance == null) {
			synchronized (DefaultRpcClient.class) {
				if (instance == null) {
					instance = new DefaultRpcClient();
				}
			}
		}
		return instance;
	}
	
	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public ConsumerProcessor getProcessor() {
		return processor;
	}

	@Override
	public void shutdown() {
		connector.shutdownGracefully();
		processor.shutdown();
	}

	@Override
	public ChannelGroupList getGroupList(String serviceID) {
		return directory.getGroupList(serviceID);
	}

	@Override
	public void addConsumerConfig(ConsumerConfig<?> consumerConfig) {
		try {
			configMap.put(consumerConfig.getInterface(),consumerConfig);		
			creatChannel(consumerConfig);
		} catch (Throwable e) {
			logger.error("creat Channel failed", e);
			ThrowUtil.throwException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void creatChannel(ConsumerConfig<?> consumerConfig) {
		//直接注册 ,不通过注册中心
		if (consumerConfig.getDirectUrl() != null) {
			String url = consumerConfig.getDirectUrl();
			UnresolvedAddress address = parseUrl(url);
			doCreantChannel(address, consumerConfig,0);
		} else {
			List<RegistryConfig> registryConfigs = consumerConfig.getRegistryRef();
			for (RegistryConfig registryConfig : registryConfigs) {
				List<Registry> registrys = RegistryFactory.getRegistry(registryConfig);
				for (Registry registry : registrys) {
					// 不copy的话 ,使用spring启动时发送的是referenceBean....
					NotifyListener listener = new DefaultNotifyListener(consumerConfig);
					ConsumerConfig newConfig = consumerConfig.copyOf(consumerConfig, ConsumerConfig.class);
					// 创建channel的动作在listener里
					try {
						registry.start();
						registry.subscribe(newConfig, listener);
					} catch (Throwable t) {
						logger.error("subscribe to {} failed ,please retry..", registry, t);
						throw t;
					}
				}
			}
		}
	}

	private void doCreantChannel(UnresolvedAddress address,ConsumerConfig<?> consumerConfig, int weight) {
		int connectionNum = consumerConfig.getConnectionNum();
		Channel channel = null;
		for (int i = 0; i < connectionNum; i++) {
			try {
				channel = connector.connect(address, false);
			} catch (Throwable t) {
				logger.error("connect to {} failed, please check the address is available or not ", address, t);
				throw t;
			}
		}
		channel.getGroup().setWeight(weight);
		ChannelGroupList groupList = directory.getGroupList(consumerConfig.getInterface());
		groupList.add(channel.getGroup());

	}

	private UnresolvedAddress parseUrl(String url) {
		int index = url.indexOf(":");
		String host = url.substring(0, index).trim();
		int port = Integer.valueOf(url.substring(index + 1).trim());
		return new UnresolvedSocketAddress(host, port);
	}

	public  class DefaultNotifyListener implements NotifyListener, java.io.Serializable {
		private static final long serialVersionUID = -6425896640822350525L;
		private transient ConsumerConfig<?> consumerConfig;

		public DefaultNotifyListener(ConsumerConfig<?> consumerConfig) {
			this.consumerConfig = consumerConfig;
		}

		@Override
		public void providerOnLine(ProviderInfo info) {
			String host = info.getHost();
			int port = info.getPort();
			int weight = info.getWeight();
			UnresolvedAddress address = new UnresolvedSocketAddress(host, port);
			doCreantChannel(address, consumerConfig,weight);
		}

		@Override
		public void providerOffLine(ProviderInfo providerInfo) {
			int port = providerInfo.getPort();
			String host = providerInfo.getHost();
			UnresolvedAddress address = new UnresolvedSocketAddress(host, port);
			DefaultRpcClient.this.directory.removeChannelGroup(address);
		}

		@Override
		public void routerAdd(String router) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void routerDelete(String router) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void configAdd(String config) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ConfigDelete(String config) {
			// TODO Auto-generated method stub
			
		}
	}
}

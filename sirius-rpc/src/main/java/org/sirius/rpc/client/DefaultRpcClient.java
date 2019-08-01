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
import org.sirius.rpc.consumer.cluster.AbstractCluster;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.ProviderInfoGroup;
import org.sirius.rpc.registry.ProviderInfoListener;
import org.sirius.rpc.registry.Registry;
import org.sirius.rpc.registry.RegistryFactory;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.DirectoryGroupList;
import org.sirius.transport.api.channel.GroupListDirectory;
import org.sirius.transport.netty.NettyTcpConnector;

public class DefaultRpcClient implements RpcClient {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRpcClient.class);
	private volatile static RpcClient instance;
	private ConcurrentMap<Class<?>, ConsumerConfig<?>> configs = Maps.newConcurrentMap();
	private ConcurrentMap<Class<?>, Invoker<?>> invokers = Maps.newConcurrentMap();
	private GroupListDirectory directory = new GroupListDirectory();
	private Connector connector;
	private ConsumerProcessor processor;
	private ProviderInfoListener listener;

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
	public void Start() {
	}

	@Override
	public Connector getConnector() {
		return this.connector;
	}

	@Override
	public ConsumerProcessor getProcessor() {
		return this.processor;
	}

	@Override
	public void Shutdown() {

	}

	@Override
	public ChannelGroupList getGroupList(String serviceID) {
		return directory.getGroupList(serviceID);
	}

	@Override
	public void addConsumerConfig(ConsumerConfig<?> consumerConfig){
		try {
			creatChannel(consumerConfig);
		} catch (Throwable e) {
			logger.error("creat Channel failed", e);
			ThrowUtil.throwException(e);
		}
	}

	private void creatChannel(ConsumerConfig<?> consumerConfig) {
		ChannelGroupList groupList = directory.getGroupList(consumerConfig.getInterface());
		if (consumerConfig.getDirectUrl() != null) {
			String url = consumerConfig.getDirectUrl();
			UnresolvedAddress address = parseUrl(url);
			doCreantChannel(connector, address, consumerConfig, groupList);

		} else {
			List<RegistryConfig> registryConfigs = consumerConfig.getRegistryRef();
			listener = new DefaultProviderInfoListener(connector, groupList, consumerConfig);
			for (RegistryConfig registryConfig : registryConfigs) {
				List<Registry> registrys = RegistryFactory.getRegistry(registryConfig);
				for (Registry registry : registrys) {
					// 不copy的话 ,使用spring启动时发送的是referenceBean....
					ConsumerConfig newConfig = consumerConfig.copyOf(consumerConfig, ConsumerConfig.class);
					// 创建channel的动作在listener里
					try {
						registry.subscribe(newConfig, listener);
					} catch (Throwable t) {
						logger.error("subscribe to {} failed ,please retry..", registry, t);
						throw t;
					}
					//等一会防止channel还没创建好就执行操作了
					synchronized(consumerConfig) {
						try {
							consumerConfig.wait(5000);
						} catch (InterruptedException e) {
							//no op
						}
					}
				}
			}
		}
	}

	private static void doCreantChannel(Connector connector, UnresolvedAddress address, ConsumerConfig<?> consumerConfig,
			ChannelGroupList groupList) {
		int connectionNum = consumerConfig.getConnectionNum();
		try {
			for (int i = 0; i < connectionNum; i++) {
				Channel channel = connector.connect(address, false);
				groupList.add(channel.getGroup());
			}
		} catch (Throwable t) {
			logger.error("connect to {} failed, please check the address is available or not ", address, t);
			throw t;
		}
	}

	private UnresolvedAddress parseUrl(String url) {
		int index = url.indexOf(":");
		String host = url.substring(0, index).trim();
		int port = Integer.valueOf(url.substring(index + 1).trim());
		return new UnresolvedSocketAddress(host, port);
	}

	public static class DefaultProviderInfoListener implements ProviderInfoListener, java.io.Serializable {
		private static final long serialVersionUID = -6425896640822350525L;
		private transient Connector connector;
		private transient ChannelGroupList groupList;
		private transient ConsumerConfig<?> consumerConfig;

		public DefaultProviderInfoListener(Connector connector, ChannelGroupList groupList,
				ConsumerConfig<?> consumerConfig) {
			this.connector = connector;
			this.groupList = groupList;
			this.consumerConfig = consumerConfig;
		}

		@Override
		public void notifyOnLine(ProviderInfoGroup providerInfoGroup) {
			List<ProviderInfo> infos = providerInfoGroup.getProviderInfos();
			for (ProviderInfo info : infos) {
				String host = info.getHost();
				int port = info.getPort();
				UnresolvedAddress address = new UnresolvedSocketAddress(host, port);
				doCreantChannel(connector, address, consumerConfig, groupList);
			}
			System.out.print("sdfff但是发发发发发发发发发发发");
			consumerConfig.notifyAll();
		}

		@Override
		public void notifyOffLine(ProviderInfoGroup providerInfoGroup) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyConfiguration(ProviderInfoGroup providerInfoGroup) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyRouter(ProviderInfoGroup providerInfoGroup) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyUpdate(ProviderInfoGroup providerInfoGroup) {
			// TODO Auto-generated method stub

		}

	}
}

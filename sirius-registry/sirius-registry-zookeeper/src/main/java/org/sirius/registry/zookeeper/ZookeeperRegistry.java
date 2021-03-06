package org.sirius.registry.zookeeper;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.registry.AbstractRegistry;
import org.sirius.rpc.registry.NotifyListener;
import org.sirius.rpc.registry.ProviderInfo;

@SuppressWarnings("rawtypes")
@Extension(value = "zookeeper", singleton = true)
public class ZookeeperRegistry extends AbstractRegistry {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZookeeperRegistry.class);
	private static final String PATH_SEPARATOR = "/";
	private String rootPath;
	private CuratorFramework zkClient;
	/**
	 * 配置项：是否本地优先
	 */
	public final static String PARAM_PREFER_LOCAL_FILE = "preferLocalFile";

	/**
	 * 配置项：是否使用临时节点。<br>
	 * 如果使用临时节点：那么断开连接的时候，将zookeeper将自动消失。好处是如果服务端异常关闭，也不会有垃圾数据。<br>
	 * 坏处是如果和zookeeper的网络闪断也通知客户端，客户端以为是服务端下线<br>
	 * 如果使用永久节点：好处：网络闪断时不会影响服务端，而是由客户端进行自己判断长连接<br>
	 * 坏处：服务端如果是异常关闭（无反注册），那么数据里就由垃圾节点，得由另外的哨兵程序进行判断
	 */
	public final static String PARAM_CREATE_EPHEMERAL = "createEphemeral";

	private boolean preferLocalFile = false;

	/**
	 * Create EPHEMERAL node when true, otherwise PERSISTENT
	 *
	 * @see ZookeeperRegistry#PARAM_CREATE_EPHEMERAL
	 * @see CreateMode#PERSISTENT
	 * @see CreateMode#EPHEMERAL
	 */
	private boolean ephemeralNode = true;

	/**
	 * 保存服务发布者的url
	 */
	private ConcurrentMap<ProviderConfig, List<String>> providerUrls = new ConcurrentHashMap<ProviderConfig, List<String>>();

	/**
	 * 保存服务消费者的url
	 */

	private ConcurrentMap<ConsumerConfig, String> consumerUrls = new ConcurrentHashMap<ConsumerConfig, String>();

	/**
	 * 服务被下线
	 */
	private final static byte[] PROVIDER_OFFLINE = new byte[] { 0 };
	/**
	 * 正常在线服务
	 */
	private final static byte[] PROVIDER_ONLINE = new byte[] { 1 };

	/**
	 * 接口配置{ConsumerConfig：PathChildrenCache} <br>
	 * 例如：{ConsumerConfig ： PathChildrenCache }
	 */
	private static final ConcurrentMap<ConsumerConfig, PathChildrenCache> INTERFACE_PROVIDER_CACHE = new ConcurrentHashMap<ConsumerConfig, PathChildrenCache>();
	private static final ConcurrentMap<ConsumerConfig, PathChildrenCache> INTERFACE_ROUTER_CACHE = new ConcurrentHashMap<ConsumerConfig, PathChildrenCache>();
	private static final ConcurrentMap<ConsumerConfig, PathChildrenCache> INTERFACE_CONFIG_CACHE = new ConcurrentHashMap<ConsumerConfig, PathChildrenCache>();

	public ZookeeperRegistry(RegistryConfig config) {
		super(config);
		init();
	}

	@Override
	protected void init() {
		if (zkClient != null) {
			return;
		}
		String addressInput = registryConfig.getAddress(); // xxx:2181,yyy:2181/path1/paht2
		if (StringUtils.isEmpty(addressInput)) {
			throw new RuntimeException("Address of zookeeper registry is empty.");
		}
		int idx = addressInput.indexOf(PATH_SEPARATOR);
		String address; // IP地址
		if (idx > 0) {
			address = addressInput.substring(0, idx);
			rootPath = addressInput.substring(idx);
			if (!rootPath.endsWith(PATH_SEPARATOR)) {
				rootPath += PATH_SEPARATOR; // 保证以"/"结尾
			}
		} else {
			address = addressInput;
			rootPath = PATH_SEPARATOR;
		}
		preferLocalFile = !CommonUtils.isFalse(registryConfig.getParameter(PARAM_PREFER_LOCAL_FILE));
		ephemeralNode = !CommonUtils.isFalse(registryConfig.getParameter(PARAM_CREATE_EPHEMERAL));
		if (logger.isInfoEnabled()) {
			logger.info("Init ZookeeperRegistry with address {}, root path is {}. preferLocalFile:{}, ephemeralNode:{}",
					address, rootPath, preferLocalFile, ephemeralNode);
		}
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFrameworkFactory.Builder zkClientuilder = CuratorFrameworkFactory.builder().connectString(address)
				.sessionTimeoutMs(registryConfig.getConnectTimeout() * 3)
				.connectionTimeoutMs(registryConfig.getConnectTimeout()).canBeReadOnly(false).retryPolicy(retryPolicy)
				.defaultData(null);

		// 是否需要添加zk的认证信息
		List<AuthInfo> authInfos = buildAuthInfo();
		if (CommonUtils.isNotEmpty(authInfos)) {
			zkClientuilder = zkClientuilder.aclProvider(getDefaultAclProvider()).authorization(authInfos);
		}

		zkClient = zkClientuilder.build();

		zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {

				if (logger.isInfoEnabled()) {
					logger.info("reconnect to zookeeper,recover provider and consumer data");
				}
				if (newState == ConnectionState.RECONNECTED) {
				}
			}
		});
	}

	public synchronized boolean start() {
		if (zkClient == null) {
			logger.warn("Start zookeeper registry must be do init first!");
			return false;
		}
		if (zkClient.getState() == CuratorFrameworkState.STARTED) {
			return true;
		}
		try {
			zkClient.start();
		} catch (Exception e) {
			throw new RuntimeException("Failed to start zookeeper zkClient", e);
		}
		return zkClient.getState() == CuratorFrameworkState.STARTED;
	}

	public void destroy() {
		if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
			zkClient.close();
		}
		providerUrls.clear();
		consumerUrls.clear();
	}

	@Override
	public void shutdown() {
		closePathChildrenCache(INTERFACE_PROVIDER_CACHE);
		closePathChildrenCache(INTERFACE_CONFIG_CACHE);
		closePathChildrenCache(INTERFACE_ROUTER_CACHE);
		zkClient.close();
		providerUrls.clear();
		consumerUrls.clear();

	}

	private void closePathChildrenCache(ConcurrentMap<ConsumerConfig, PathChildrenCache> map) {
		for (Map.Entry<ConsumerConfig, PathChildrenCache> entry : map.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				logger.error("Close PathChildrenCache error!", e);
			}
		}
	}

	@Override
	protected void doRegister(ProviderConfig<?> config) {
		// 注册服务端节点
		try {
			// 避免重复计算
			List<String> urls;
			if (providerUrls.containsKey(config)) {
				urls = providerUrls.get(config);
			} else {
				urls = ZookeeperRegistryHelper.convertProviderToUrls(config);
				providerUrls.put(config, urls);
			}
			if (CommonUtils.isNotEmpty(urls)) {
				String providerPath = ZookeeperRegistryHelper.buildProviderPath(rootPath, config);
				for (String url : urls) {
					url = URLEncoder.encode(url, "UTF-8");
					String providerUrl = providerPath + PATH_SEPARATOR + url;

					try {
						getAndCheckZkClient().create().creatingParentContainersIfNeeded()
								.withMode(ephemeralNode ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT) // 是否永久节点
								.forPath(providerUrl, config.isDynamic() ? PROVIDER_ONLINE : PROVIDER_OFFLINE); // 是否默认上下线
					} catch (KeeperException.NodeExistsException nodeExistsException) {
						if (logger.isWarnEnabled()) {
							logger.warn("provider has exists in zookeeper, provider=" + providerUrl);
						}
					}
				}

				if (logger.isInfoEnabled()) {
					logger.info("registry provider :{} successful", providerPath);
				}

			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to register provider to zookeeperRegistry!", e);
		}
	}

	@Override
	protected void doSubscribe(ConsumerConfig<?> config, NotifyListener listener) {
		subscribeConsumerUrls(config);
		subscribeProvider(config, listener);
		subscribeConfig(config, listener);
		subscribeRouter(config, listener);
	}

	protected void subscribeConsumerUrls(ConsumerConfig config) {
		// 注册Consumer节点
		String url = null;
		if (config.isRegister()) {
			try {
				String consumerPath = ZookeeperRegistryHelper.buildConsumerPath(rootPath, config);
				if (consumerUrls.containsKey(config)) {
					url = consumerUrls.get(config);
				} else {
					url = ZookeeperRegistryHelper.convertConsumerToUrl(config);
					consumerUrls.put(config, url);
				}
				String encodeUrl = URLEncoder.encode(url, "UTF-8");
				getAndCheckZkClient().create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL) // Consumer临时节点
						.forPath(consumerPath + PATH_SEPARATOR + encodeUrl);

			} catch (KeeperException.NodeExistsException nodeExistsException) {
				if (logger.isWarnEnabled()) {
					logger.warn("consumer has exists in zookeeper, consumer=" + url);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to register consumer to zookeeperRegistry!", e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void subscribeProvider(ConsumerConfig<?> config, NotifyListener listener) {
		final String providerPath = ZookeeperRegistryHelper.buildProviderPath(rootPath, config);
		PathChildrenCache pathChildrenCache = INTERFACE_PROVIDER_CACHE.get(config);
		try {
			if (pathChildrenCache == null) {
				pathChildrenCache = new PathChildrenCache(zkClient, providerPath, true);
				pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
					@Override
					public void childEvent(CuratorFramework client1, PathChildrenCacheEvent event) throws Exception {
						if (logger.isDebugEnabled()) {
							logger.debug(config.getAppName(),
									"Receive zookeeper event: " + "type=[" + event.getType() + "]");
						}
						ChildData childData = event.getData();
						if (childData != null) {
							ProviderInfo providerInfo = (ProviderInfo) ZookeeperRegistryHelper
									.convertUrlToProvider(providerPath, childData);
							switch (event.getType()) {
							case CHILD_ADDED: // 加了一个provider
								listener.providerOnLine(providerInfo);
								break;
							case CHILD_REMOVED: // 删了一个provider
								listener.providerOffLine(providerInfo);
								break;
							case CHILD_UPDATED: // 更新一个Provider
								break;
							default:
								break;
							}
						}
					}
				});
				pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
				List<ProviderInfo> providerInfos = ZookeeperRegistryHelper.convertUrlsToProviders(providerPath,
						pathChildrenCache.getCurrentData());
				for (ProviderInfo info : providerInfos) {
					listener.providerOnLine(info);
				}
				INTERFACE_PROVIDER_CACHE.put(config, pathChildrenCache);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to subscribe provider from zookeeperRegistry!", e);
		}
	}

	@SuppressWarnings("deprecation")
	private void subscribeConfig(ConsumerConfig<?> config, NotifyListener listener) {
		final String configPath = ZookeeperRegistryHelper.buildConfigPath(rootPath, config);
		PathChildrenCache pathChildrenCache = INTERFACE_CONFIG_CACHE.get(config);
		try {
			if (pathChildrenCache == null) {
				pathChildrenCache = new PathChildrenCache(zkClient, configPath, true);
				pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
					@Override
					public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
						if (logger.isDebugEnabled()) {
							logger.debug(config.getAppName(),
									"Receive zookeeper event: " + "type=[" + event.getType() + "]");
						}
						ChildData childData = event.getData();
						if (childData != null) {

							switch (event.getType()) {
							case CHILD_ADDED:
								break;
							case CHILD_REMOVED:
								break;
							case CHILD_UPDATED:
								break;
							default:
								break;
							}
						}
					}
				});
				pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

				INTERFACE_CONFIG_CACHE.put(config, pathChildrenCache);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to subscribe config from zookeeperRegistry!", e);
		}
	}

	@SuppressWarnings("deprecation")
	private void subscribeRouter(ConsumerConfig<?> config, NotifyListener listener) {
		final String routerPath = ZookeeperRegistryHelper.buildRouterPath(rootPath, config);
		PathChildrenCache pathChildrenCache = INTERFACE_ROUTER_CACHE.get(config);
		try {
			if (pathChildrenCache == null) {
				pathChildrenCache = new PathChildrenCache(zkClient, routerPath, true);
				pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
					@Override
					public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
						if (logger.isDebugEnabled()) {
							logger.debug(config.getAppName(),
									"Receive zookeeper event: " + "type=[" + event.getType() + "]");
						}
						ChildData childData = event.getData();
						if (childData != null) {
							String router = ZookeeperRegistryHelper.convertUrlToRouter(routerPath, childData);
							switch (event.getType()) {
							case CHILD_ADDED:
								listener.routerAdd(router);
								break;
							case CHILD_REMOVED: //
								listener.routerDelete(router);
								break;
							default:
								break;
							}
						}
					}
				});
				pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
				List<String> routers = ZookeeperRegistryHelper.convertUrlsToRouter(routerPath,
						pathChildrenCache.getCurrentData());
				for (String router : routers) {
					listener.routerAdd(router);
				}
				INTERFACE_ROUTER_CACHE.put(config, pathChildrenCache);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to subscribe provider from zookeeperRegistry!", e);
		}
	}

	@Override
	protected void doUnSubscribe(ConsumerConfig config) {

		try {
			String url = consumerUrls.remove(config);
			if (url != null) {
				String consumerPath = ZookeeperRegistryHelper.buildConsumerPath(rootPath, config);
				url = URLEncoder.encode(url, "UTF-8");
				getAndCheckZkClient().delete().forPath(consumerPath + PATH_SEPARATOR + url);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to unregister consumer to zookeeperRegistry!", e);

		}
		PathChildrenCache providerCache = INTERFACE_PROVIDER_CACHE.remove(config);
		PathChildrenCache routerCache = INTERFACE_ROUTER_CACHE.remove(config);
		PathChildrenCache configCache = INTERFACE_PROVIDER_CACHE.remove(config);
		try {
			if (providerCache != null) {
				providerCache.close();
			}
			if (routerCache != null) {
				routerCache.close();
			}
			if (configCache != null) {
				configCache.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to unsubscribe consumer config from zookeeperRegistry!", e);
		}
	}

	@Override
	protected void doUnregister(ProviderConfig config) {
		try {
			List<String> urls = providerUrls.remove(config);
			if (CommonUtils.isNotEmpty(urls)) {
				String providerPath = ZookeeperRegistryHelper.buildProviderPath(rootPath, config);
				for (String url : urls) {
					url = URLEncoder.encode(url, "UTF-8");
					getAndCheckZkClient().delete().forPath(providerPath + PATH_SEPARATOR + url);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to unregister provider to zookeeperRegistry!", e);
		}
	}

	/**
	 * 获取默认的AclProvider
	 * 
	 * @return
	 */
	private ACLProvider getDefaultAclProvider() {
		return new ACLProvider() {
			@Override
			public List<ACL> getDefaultAcl() {
				return ZooDefs.Ids.CREATOR_ALL_ACL;
			}

			@Override
			public List<ACL> getAclForPath(String path) {
				return ZooDefs.Ids.CREATOR_ALL_ACL;
			}
		};
	}

	/**
	 * 创建认证信息
	 * 
	 * @return
	 */
	private List<AuthInfo> buildAuthInfo() {
		List<AuthInfo> info = new ArrayList<AuthInfo>();

		String scheme = registryConfig.getParameter("scheme");

		// 如果存在多个认证信息，则在参数形式为为addAuth=user1:paasswd1,user2:passwd2
		String addAuth = registryConfig.getParameter("addAuth");

		if (StringUtils.isNotEmpty(addAuth)) {
			String[] addAuths = addAuth.split(",");
			for (String singleAuthInfo : addAuths) {
				info.add(new AuthInfo(scheme, singleAuthInfo.getBytes()));
			}
		}

		return info;
	}

	protected CuratorFramework getZkClient() {
		return zkClient;
	}

	private CuratorFramework getAndCheckZkClient() {
		if (zkClient == null || zkClient.getState() != CuratorFrameworkState.STARTED) {
			throw new RuntimeException("Zookeeper client is not available");
		}
		return zkClient;
	}

}

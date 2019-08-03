package org.sirius.registry.zookeeper;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.rpc.registry.AbstractRegistry;
import org.sirius.rpc.registry.ProviderInfoListener;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class zookeeperRegistry extends AbstractRegistry {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(zookeeperRegistry.class);
	private static final String PATH_SEPARATOR = "/";
	private CuratorFramework zkClient;

	public zookeeperRegistry(RegistryConfig config) {
		super(config);

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFrameworkFactory.Builder zkClientbuilder = CuratorFrameworkFactory.builder()
				.connectString("127.0.0.1:2181")
				.sessionTimeoutMs(60 * 1000)
				.connectionTimeoutMs(15 * 1000)
				.canBeReadOnly(false)
				.retryPolicy(retryPolicy)
				.namespace("sirius")
				.defaultData(null);
		zkClient = zkClientbuilder.build();
		zkClient.getConnectionStateListenable().addListener((client,newState)->{
			
			logger.info("zookeeper connection state changed to {} " ,newState);
			if(newState == ConnectionState.RECONNECTED) {
				logger.info("zookeeper  reconnected ,need to re-registed or re-subscribe" );
				
			}
		});
		zkClient.start();
	}

	@Override
	protected void init() {

	}

	@Override
	protected void doRegister(ProviderConfig providerConfig) {

		List<String> paths = buildProviderPath(providerConfig);
		for(String path : paths) {
			try {
				zkClient.create()
				        .creatingParentContainersIfNeeded()
				        .withMode(CreateMode.EPHEMERAL)
				        .forPath(path);
			} catch (Exception e) {
				logger.warn("failed to register {} ", path ,e);
			}
		}
	}

	private List<String> buildProviderPath(ProviderConfig providerConfig) {
		
		List<String> providerUrls = configToUrl(providerConfig);
		List<String> paths = new ArrayList<String>();
		for(String url : providerUrls) {
			StringBuilder builder = new StringBuilder();
			builder.append(PATH_SEPARATOR)
			       .append(providerConfig.getInterface() + PATH_SEPARATOR)
			       .append("providers" + PATH_SEPARATOR)
			       .append(url);
			paths.add(builder.toString());
		}
		return  paths;
	}

	private List<String> configToUrl(ProviderConfig<?> providerConfig) {
		List<String> urls = new ArrayList<String>();
		List<ServerConfig> servers = providerConfig.getServerRef();
		for(ServerConfig server : servers) {
			StringBuilder builder = new StringBuilder();
			builder.append(server.getProtocol() + ":" +"\\\\")
			       .append(server.getHost() + ":")
			       .append(server.getPort());
			urls.add(builder.toString());
		}
		return urls;
	}

	@Override
	protected void doUnSubscribe(ConsumerConfig config) {

	}

	@Override
	protected void doUnregister(ProviderConfig config) {

	}

	@Override
	protected void doSubscribe(ConsumerConfig config, ProviderInfoListener listener) {

	}

	public static void main(String args[]) {
		
		ServerConfig sc = new ServerConfig();
		sc.setHost("127.0.1.1").setPort(2000).setProtocol("netty").setSerialization("hession");
		ProviderConfig pc = new ProviderConfig();
		pc.setInterface("org.sirius.hello.class");
		pc.addServer(sc);
		
		zookeeperRegistry zr = new zookeeperRegistry(new RegistryConfig());
		zr.doRegister(pc);
		  
	}
}

package org.sirius.registry;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.rpc.registry.RegistryServer;
import org.sirius.rpc.registry.RegistryService;

public class DefaultRegistryServer implements RegistryServer{

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryServer.class);

	private final static int DEFAULT_PORT = 20080;
	private final static String protocol = "netty";

	private int port;
	
	private ProviderConfig<DefaultRegistryService> config;
	
	public DefaultRegistryServer() {
		this(DEFAULT_PORT);
	}
	public DefaultRegistryServer(int port) {
		this.port = port;
		init();
	}


	@SuppressWarnings("rawtypes")
	private void init() {
		config = new ProviderConfig<DefaultRegistryService>();
		ServerConfig server = new ServerConfig();
		server.setPort(port)
		      .setProtocol(protocol);
		config.setInterface(RegistryService.class.getName())
		      .setRef(new DefaultRegistryService())
		      .addServer(server);
	}


	@Override
	public void start() {
		config.export();
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	public static void main(String args[])  {

		DefaultRegistryServer server = new DefaultRegistryServer();
		server.start();
		
	}
}

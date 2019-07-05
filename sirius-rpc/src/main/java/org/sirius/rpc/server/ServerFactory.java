package org.sirius.rpc.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.ext.ExtensionClass;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.NetUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.SystemInfo;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ServerConfig;

public class ServerFactory {

    private final static InternalLogger   LOGGER     = InternalLoggerFactory.getInstance(ServerFactory.class);
    private final static ConcurrentMap<String, RpcServer> SERVER_MAP = new ConcurrentHashMap<String, RpcServer>(); 
    
    /**
     * 初始化Server实例
     *
     * @param serverConfig 服务端配置
     * @return Server
     */
    public synchronized static RpcServer getServer(ServerConfig serverConfig) {
        try {
        	RpcServer server = SERVER_MAP.get(Integer.toString(serverConfig.getPort()));
            if (server == null) {
                // 算下网卡和端口
                resolveServerConfig(serverConfig);

                ExtensionClass<RpcServer> ext = ExtensionLoaderFactory.getExtensionLoader(RpcServer.class).getExtensionClass(serverConfig.getProtocol());
                if (ext == null) {
                    throw new RuntimeException("server.protocol " + serverConfig.getProtocol() + " is Unsupported !");
                }
                server = ext.getExtInstance();
                server.init(serverConfig);
                SERVER_MAP.put(serverConfig.getPort() + "", server);
            }
            return server;
        }catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

	private static void resolveServerConfig(ServerConfig serverConfig) {
		 // 绑定到指定网卡 或全部网卡
        String boundHost = serverConfig.getBoundHost();
        if (boundHost == null) {
            String host = serverConfig.getHost();
            if (StringUtils.isBlank(host)) {
                host = SystemInfo.getLocalHost();
                serverConfig.setHost(host);
                // windows绑定到0.0.0.0的某个端口以后，其它进程还能绑定到该端口
                boundHost = SystemInfo.isWindows() ? host : NetUtils.ANYHOST;
            } else {
                boundHost = host;
            }
            serverConfig.setBoundHost(boundHost);
        }

        // 绑定的端口
        if (serverConfig.isAdaptivePort()) {
            int oriPort = serverConfig.getPort();
            int port = NetUtils.getAvailablePort(boundHost, oriPort,65536);
            if (port != oriPort) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Changed port from {} to {} because the config port is disabled", oriPort, port);
                }
                serverConfig.setPort(port);
            }
        }
	}
}

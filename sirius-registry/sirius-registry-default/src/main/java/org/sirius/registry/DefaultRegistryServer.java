package org.sirius.registry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.sirius.registry.api.RegistryService;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.ProviderProxyUtil;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.netty.NettyTcpAcceptor;

public class DefaultRegistryServer extends NettyTcpAcceptor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryServer.class);

	private static int default_port = 20080;
	private ProviderProcessor processor = new RegistryProcessor();
	private ProviderProxyInvoker<?> registryInvoker;
	private RegistryService registryService = new DefaultRegistryService();

	DefaultRegistryServer() {
		this(default_port);
	}

	DefaultRegistryServer(int port) {
		super(port);
		this.setProcessor(processor);
		registryInvoker = (ProviderProxyInvoker<?>) ProviderProxyUtil.getInvoker(registryService, RegistryService.class);
	}

	@SuppressWarnings("rawtypes")
	class RegistryProcessor implements ProviderProcessor {

		@Override
		public void handlerRequest(Channel channel, Request request) {
			
			int length;
			if ((length = request.getParametersType().length) != 1) {
				logger.warn("非法参数个数 , 参数个数为 {}", length);
				return;
			}

			Object param = request.getParameters()[0];
			Class type = param.getClass();
			
			String serviceName ;
			InetSocketAddress address;
			DefaultRegistryService _registryService = (DefaultRegistryService) registryService;
			
			if (type.equals(ProviderConfig.class)) {
				
				ProviderConfig pconfig = (ProviderConfig) param;
				serviceName = pconfig.getUniqueId();
				address =(InetSocketAddress) channel.remoteAddress();
				ConcurrentHashSet<String> addressList =  _registryService.providers.get(serviceName);
				
				if(addressList == null) {
					addressList = new ConcurrentHashSet<String>();
					_registryService.providers.putIfAbsent(serviceName, addressList);
				}
				//重新取一次,避免并发问题
				addressList = _registryService.providers.get(serviceName);
				
				
			}else if(type.equals(ConsumerConfig.class)){
				ConsumerConfig Cconfig = (ConsumerConfig)param;
				
			}else {
				logger.warn("非法参数类型 , 参数类型为 {}", type);
			}

			Response res = null;
			try {
				res = registryInvoker.invoke(request);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				channel.send(res);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void handlerException(Channel channel, Throwable e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void shutdown() {
			// TODO Auto-generated method stub

		}

	}
	
	
}

package org.sirius.registry;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.Maps;
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
import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.handler.acceptor.AcceptorHandler;

import io.netty.channel.ChannelHandlerContext;

public class DefaultRegistryServer extends NettyTcpAcceptor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryServer.class);

	private static int default_port = 20080;

	private ProviderProcessor processor = new RegistryProcessor();
	private ProviderProxyInvoker<?> registryInvoker;
	private RegistryService registryService = new DefaultRegistryService();

	// 订阅者保持的链接 , key ->订阅者ip,不包括port
	ConcurrentMap<String, ConcurrentHashSet<Channel>> consumerChannelsMap = Maps.newConcurrentMap();
	// 发布者持有的链接, key ->发布者ip,不包括port
	ConcurrentMap<String, ConcurrentHashSet<Channel>> providerChannelsMap = Maps.newConcurrentMap();

	DefaultRegistryServer() {
		this(default_port);
	}

	DefaultRegistryServer(int port) {
		super(port);
		this.setProcessor(processor);
		registryInvoker = (ProviderProxyInvoker<?>) ProviderProxyUtil.getInvoker(registryService,
				RegistryService.class);
	}

	
	class RegistryHandler extends AcceptorHandler {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			
			super.channelActive(ctx);
			
			io.netty.channel.Channel ch = ctx.channel();
			NettyChannel nc = ch.attr(NettyChannel.NETTY_CHANNEL_KEY).get();
			String remoteHost = getRemoteHost(ch);
			
			//添加订阅者的链接
			ConcurrentHashSet<Channel> consumerChannels = consumerChannelsMap.get(remoteHost);
			if(consumerChannels != null) {
				consumerChannels.add(nc);
			}
			
			//添加发布者的链接
			ConcurrentHashSet<Channel> providerChannels = providerChannelsMap.get(remoteHost);
			if(providerChannels != null) {
				providerChannels.add(nc);
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			
			io.netty.channel.Channel ch = ctx.channel();
			NettyChannel nc = ch.attr(NettyChannel.NETTY_CHANNEL_KEY).get();
			
			String remoteHost = getRemoteHost(ch);
			
			ConcurrentHashSet<Channel> consumerChannels = consumerChannelsMap.get(remoteHost);
			if(consumerChannels.remove(nc)) {
				if(consumerChannels.size() == 0) {
					consumerChannelsMap.remove(remoteHost);
				}
				return ;
			}
			
			ConcurrentHashSet<Channel> providerChannels = providerChannelsMap.get(remoteHost);
			providerChannels.remove(nc);
			if(providerChannels.size() == 0) {
				providerChannelsMap.remove(remoteHost);
			}
		}
		
		private String getRemoteHost(io.netty.channel.Channel ch) {
			InetSocketAddress address = (InetSocketAddress) ch.remoteAddress();
			return address.getHostString();
		}
	}

	@SuppressWarnings("rawtypes")
	class RegistryProcessor implements ProviderProcessor {

		@Override
		public void handlerRequest(Channel channel, Request request) {
			checkRequest(request);
			String remoteHost = getRemoteHost(channel);
			Object param = request.getParameters()[0];
			Class type = param.getClass();
			String serviceName;
			DefaultRegistryService _registryService = (DefaultRegistryService) registryService;

			if (type.equals(ProviderConfig.class)) {
				ProviderConfig pconfig = (ProviderConfig) param;
				serviceName = pconfig.getUniqueId();

				ConcurrentHashSet<String> hostList = _registryService.providers.get(serviceName);
				if (hostList == null) {
					hostList = new ConcurrentHashSet<String>();
					_registryService.providers.putIfAbsent(serviceName, hostList);
				}
				// 重新取一次,避免并发问题
				hostList = _registryService.providers.get(serviceName);
				hostList.add(remoteHost);
				
				//添加发布者链接
				ConcurrentHashSet<Channel> providerChannels = providerChannelsMap.get(remoteHost);
				if(providerChannels == null) {
					providerChannels = new ConcurrentHashSet<Channel>();
					providerChannelsMap.putIfAbsent(remoteHost, providerChannels);
				}
				providerChannels = providerChannelsMap.get(remoteHost);
				providerChannels.add(channel);
				

			} else {
				ConsumerConfig Cconfig = (ConsumerConfig) param;
				serviceName = Cconfig.getUniqueId();

				ConcurrentHashSet<String> hostList = _registryService.consumers.get(serviceName);
				if (hostList == null) {
					hostList = new ConcurrentHashSet<String>();
					_registryService.consumers.putIfAbsent(serviceName, hostList);
				}
				// 重新取一次,避免并发问题
				hostList = _registryService.consumers.get(serviceName);
				hostList.add(remoteHost);
				
				//添加订阅者的链接
				ConcurrentHashSet<Channel> consumerChannels = consumerChannelsMap.get(remoteHost);
				if(consumerChannels != null) {
					consumerChannels = new ConcurrentHashSet<Channel>();
					consumerChannelsMap.putIfAbsent(remoteHost, consumerChannels);
				}
				consumerChannels = consumerChannelsMap.get(remoteHost);
				consumerChannels.add(channel);
			}

			Response res = null;
			try {
				res = registryInvoker.invoke(request);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			try {
				channel.send(res);
			} catch (Exception e) {
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

		private String getRemoteHost(Channel channel) {

			NettyChannel _channel = (NettyChannel) channel;
			io.netty.channel.Channel nettyChannel = _channel.nettyChannel();
			InetSocketAddress address = (InetSocketAddress) nettyChannel.remoteAddress();
			return address.getHostString();
		}

		private void checkRequest(Request request) {

			Object[] types = request.getParametersType();
			int length = types.length;
			Object type = types[0];
			if (length != 1 || (!type.equals(ConsumerConfig.class) && !type.equals(ProviderConfig.class))) {
				logger.warn("无效的参数个数 或者 类型 .");
				return;
			}
		}

	}

}

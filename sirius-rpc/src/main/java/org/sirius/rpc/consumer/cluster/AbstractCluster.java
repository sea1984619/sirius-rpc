package org.sirius.rpc.consumer.cluster;

import java.io.Serializable;
import java.util.List;

import org.sirius.common.ext.Extensible;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.cluster.router.Router;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.future.InvokeFuture;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.load.balance.LoadBalancer;
import org.sirius.rpc.load.balance.RandomLoadBalancer;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.ProviderInfoGroup;
import org.sirius.rpc.registry.ProviderInfoListener;
import org.sirius.rpc.registry.Registry;
import org.sirius.rpc.registry.RegistryFactory;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.DirectoryGroupList;
import org.sirius.transport.netty.NettyTcpConnector;

@Extensible
public class AbstractCluster<T> extends Cluster<T> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCluster.class);
	private Router router;
	private LoadBalancer<ChannelGroup> loadBalancer = new RandomLoadBalancer<ChannelGroup>();;
	private ConsumerProcessor consumerProcessor = new DefaultConsumerProcessor();
	private ProviderInfoListener listener ;
	private RpcClient client;

	public AbstractCluster(ConsumerConfig<T> consumerConfig,RpcClient client) {
		super(consumerConfig);
		this.client =client;
		
	}

	public void setConsumerConfig(ConsumerConfig<T> consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response invoke(Request request) throws Throwable {

		Response response = null;
		String invokeType = request.getInvokeType();
		DefaultInvokeFuture<Response> future;
		Channel channel;
		try {
			ChannelGroupList channelGroupList = client.getGroupList(consumerConfig.getInterface());
			ChannelGroup group = loadBalancer.select(channelGroupList.getChannelGroup());
			channel = group.next();
			channel.send(request);
			RpcInvokeContent.getContent().setFuture(null);
			int timeout = request.getTimeout();
			// 同步调用
			if (invokeType.equals(RpcConstants.INVOKER_TYPE_SYNC)) {
				try {
					future = new DefaultInvokeFuture<Response>(channel, request, timeout, null);
					response = future.getResponse();
					RpcInvokeContent.getContent().setFuture(null);
				} catch (Exception e) {
					logger.error("invocation of {}.{}get result failed, the reason maybe {}",
							request.getClassName(),request.getMethodName(), e);
					throw e;
				}
			} else if (invokeType.equals(RpcConstants.INVOKER_TYPE_FUTURE)) {
				response = buildEmptyResponse(request);
				// 异步调用 需要设置过滤链 过滤返回结果
				List<Filter> filters = consumerConfig.getFilterRef();
				future = new DefaultInvokeFuture<Response>(channel, request, timeout, filters);
				RpcInvokeContent.getContent().setFuture(future);
			}

		} catch (Throwable t) {
			logger.error("invocation of {}.{} sended failed, the reason maybe {}",
					request.getClassName(),request.getMethodName(), t);
			throw t;
		}
		RpcInvokeContent.getContent().set("channel", channel);
		return response;
	}

	private Response buildEmptyResponse(Request request) {
		Response response = new Response(request.invokeId());
		response.setResult(ClassUtil.getDefaultPrimitiveValue(request.getReturnType()));
		return response;
	}
}

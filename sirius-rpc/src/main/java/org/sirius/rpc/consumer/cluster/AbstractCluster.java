package org.sirius.rpc.consumer.cluster;
import java.util.List;

import org.sirius.common.ext.Extensible;
import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.consumer.AsyncResponse;
import org.sirius.rpc.consumer.loadbalance.LoadBalancer;
import org.sirius.rpc.consumer.router.Router;
import org.sirius.rpc.consumer.router.RouterChain;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.channel.ChannelGroupList;

@Extensible
public class AbstractCluster<T> extends AbstractInvoker<T> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCluster.class);
	private RouterChain routerChain;
	private LoadBalancer loadBalancer;
	private RpcClient client;
	private ConsumerConfig<T> consumerConfig;

	@SuppressWarnings("unchecked")
	public AbstractCluster(ConsumerConfig<T> consumerConfig, RpcClient client) {
		super(consumerConfig);
		this.client = client;
		this.consumerConfig = (ConsumerConfig<T>) getConfig();
		initLoadBalancer(consumerConfig);
		initRouterChain(consumerConfig);
	}
	
	@SuppressWarnings("unchecked")
	private void initRouterChain(ConsumerConfig<T> consumerConfig) {
		ExtensionLoader<Router>  routerLoader = ExtensionLoaderFactory.getExtensionLoader(Router.class);
		List<Router> routers = routerLoader.getAllExtensions(null, true);
		
	}

	@SuppressWarnings("unchecked")
	public void initLoadBalancer(ConsumerConfig<T> consumerConfig) {
		String loadBalancer = consumerConfig.getLoadBalancer();
		if(StringUtils.isEmpty(loadBalancer)) {
			loadBalancer = "roundRobin";
		}
		ExtensionLoader<LoadBalancer>  loadBalancerLoader = ExtensionLoaderFactory.getExtensionLoader(LoadBalancer.class);
		this.loadBalancer = (LoadBalancer) loadBalancerLoader.getExtensionClass(loadBalancer);
	}

	@Override
	public Response invoke(Request request) throws Throwable {
		Response response = null;
		String invokeType = request.getInvokeType();
		DefaultInvokeFuture<Response> future;
		Channel channel = null;
		try {
			ChannelGroupList channelGroupList = client.getGroupList(consumerConfig.getInterface());
			List<ChannelGroup> groups = routerChain.route(channelGroupList.getList(), request);
			ChannelGroup group = loadBalancer.select(groups,request);
			channel = group.next();
			channel.send(request);
			int timeout = request.getTimeout();
			// 同步调用
			if (invokeType.equals(RpcConstants.INVOKER_TYPE_SYNC)) {
				future = new DefaultInvokeFuture<Response>(channel, request, timeout, null);
				response = future.getResponse();
				RpcInvokeContent.getContent().setFuture(null);

			} else if (invokeType.equals(RpcConstants.INVOKER_TYPE_FUTURE)) {
				
				// 异步调用 需要设置过滤链 过滤返回结果
				List<Filter> filters = consumerConfig.getFilterRef();
				response = buildAsyncResponse(request, filters);
				future = new DefaultInvokeFuture<Response>(channel, request, timeout, (AsyncResponse)response);
				RpcInvokeContent.getContent().setFuture(future);

			} else if (invokeType.equals(RpcConstants.INVOKER_TYPE_ONEWAY)) {
				response = buildEmptyResponse(request);
				RpcInvokeContent.getContent().setFuture(null);

			} else {
				throw new RpcException("Unknown invoke type " + invokeType);
			}

		} catch (Throwable t) {
			logger.error("invocation of {}.{} invoked failed, the reason is {}", request.getClassName(),
					request.getMethodName(), t);
			response = buildErrorResponse(request,t);
		}
		// 方法返回时,ArgumentCallbackHandler会用到
		System.out.println(channel);
		RpcInvokeContent.getContent().set(RpcConstants.CHANNEL, channel);
		return response;
	}

	private Response buildEmptyResponse(Request request) {
		Response response = new Response(request.invokeId());
		response.setResult(ClassUtil.getDefaultPrimitiveValue(request.getReturnType()));
		return response;
	}

	private AsyncResponse buildAsyncResponse(Request request,List<Filter> filters) {
		AsyncResponse response = new AsyncResponse(request.invokeId());
		response.setFilters(filters);
		/*
		 * 这里的content只是引用, 是否需要深拷贝？假设content里存储了一些只有单次调用才生效的数据,
		 * 而在执行filter异步回调时又需要这些数据, 而因为执行回调的线程和当前线程不是一个线程,它们并发执行
		 * 那么这些数据在回调时很大可能会当前线程被更改。这种情况肯定需要深拷贝。
		 * 但是深拷贝坑比较深,不碰为好,所以filter执行回调时应该避免使用到仅单次调用有效的数据
		 */
		response.setContent(RpcInvokeContent.getContent());
		response.setResult(ClassUtil.getDefaultPrimitiveValue(request.getReturnType()));
		return response;
	}
	private Response buildErrorResponse(Request request,Throwable t) {
		Response response = new Response(request.invokeId());
		response.setResult(t);
		return response;
	}
}

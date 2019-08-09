package org.sirius.rpc.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.Maps;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.argumentcallback.ArgumentCallbackRequest;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelListener;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

/*
 * 处理有关参数回调的事项
 */
public class ArgumentCallbackHandler {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ArgumentCallbackHandler.class);
	private static final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("ArgumentCallbackHandler.timer", true));
	private static final String NEWREQUEST = "newRequst";
	/*
	 * 缓存 : key : callbackArgument.hashcode -> value : invoker
	 * 作用主要是发送同一个回调参数时,不会创建新的callback对象;
	 */
	private final static Map<Integer, Invoker> callbackInvokers = Maps.newConcurrentMap();
	/*
	 * 缓存 : key:methodName -> List<ArgumentConfig>
	 */
	private Map<String, List<ArgumentConfig>> argumentsMap = Maps.newConcurrentMap();
	/*
	 * 缓存 : key:channel -> List<ArgumentCallbackRequest> 在当前channel异常关闭后,需要重试。
	 * 将创建callbackinvoke的request保存起来,如果当前channal异常关闭,重新发送这个request,
	 * 尝试重新建立与对端callback的链接
	 */
	private Map<Channel, List<ArgumentCallbackRequest>> retryRequest = Maps.newConcurrentMap();

	private ConsumerConfig<?> consumerConfig;

	private Invoker invoker;

	@SuppressWarnings("rawtypes")
	public ArgumentCallbackHandler(ConsumerConfig consumerConfig, Invoker invoker) {
		this.consumerConfig = consumerConfig;
		this.invoker = invoker;
		init(consumerConfig);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void init(ConsumerConfig consumerConfig) {
		Map<String, MethodConfig> methods = consumerConfig.getMethods();
		if (methods != null) {
			for (MethodConfig method : methods.values()) {
				if (method.getArguments() != null)
					argumentsMap.put(method.getName(), method.getArguments());
			}
		}
	}

	public static void setCallbackInvoker(int hashcode, Invoker<?> invoker) {
		callbackInvokers.putIfAbsent(hashcode, invoker);
	}

	public static Invoker<?> getCallbackInvoker(int hashcode) {
		return callbackInvokers.get(hashcode);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Request handleRequest(Request request) {
		String methodName = request.getMethodName();
		List<ArgumentConfig> arguments = argumentsMap.get(methodName);
		boolean hasNewCallback = false;
		if (arguments != null) {
			for (ArgumentConfig argument : arguments) {
				if (argument.isCallback()) {
					int index = argument.getIndex();
					if (index < 0 || index > request.getParameters().length - 1) {
						throw new IndexOutOfBoundsException("index in <sirius:argument> is out Of bound");
					}
					// 获取callback参数
					Object callbackArument = request.getParameters()[index];
					Class clazz = callbackArument.getClass();
					if (ClassUtil.isPrimitive(clazz) || clazz.isArray() || clazz.isAssignableFrom(Collection.class)) {
						throw new IllegalStateException("the callback argument type is " + clazz + ","
								+ " must not be a Array or a Collection or a primitive type");
					}
					int hashcode = System.identityHashCode(callbackArument);
					Invoker callbackInvoker = callbackInvokers.get(hashcode);
					if (callbackInvoker == null) {
						// 将callbackArument包装为代理invoker ,以供调用
						callbackInvoker = ProxyFactory.getInvokerNotCache(callbackArument, clazz);
						callbackInvokers.putIfAbsent(hashcode, callbackInvoker);
						callbackInvoker = callbackInvokers.get(hashcode);
						argument.setId(String.valueOf(hashcode));
						hasNewCallback = true;
					}
				}
			}

			ArgumentCallbackRequest argRequset = new ArgumentCallbackRequest(request, arguments);
			if (hasNewCallback) {
				/*
				 * 先存起来,因为现在拿不到channel,等返回时拿到channel再存进retryRequest里
				 */
				RpcInvokeContent.getContent().set(NEWREQUEST, argRequset);
			}
			return argRequset;
		}
		return request;
	}

	public void onReturn() {
		ArgumentCallbackRequest requset = (ArgumentCallbackRequest) RpcInvokeContent.getContent().get(NEWREQUEST);
		if (requset != null) {
			Channel channel = (Channel) RpcInvokeContent.getContent().get(RpcConstants.CHANNEL);
			List<ArgumentCallbackRequest> requests = retryRequest.get(channel);
			if (requests == null) {
				requests = new ArrayList<ArgumentCallbackRequest>();
				retryRequest.putIfAbsent(channel, requests);
				requests = retryRequest.get(channel);
			}
			requests.add(requset);
			RpcInvokeContent.getContent().remove("newArgumentRequest");
			channel.setListener(new ChannelListener() {
				@Override
				public void onClosed(Channel channel) {
					List<ArgumentCallbackRequest> requests = retryRequest.get(channel);
					for (ArgumentCallbackRequest request : requests) {
						RetryTask task = new RetryTask(request);
						timer.newTimeout(task, 1, TimeUnit.MICROSECONDS);
					}
				}
			});
		}
	}

	private final class RetryTask implements TimerTask {

		private ArgumentCallbackRequest request;

		public RetryTask(ArgumentCallbackRequest request) {
			this.request = request;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			request.setReconnect(true);
			// 同步调用,保证结果返回;
			RpcInvokeContent.getContent().setInvokeType(RpcConstants.INVOKER_TYPE_SYNC);
			RpcInvokeContent.getContent().setTimeout(3000);
			try {
				invoker.invoke(request);
			} catch (Throwable e) {
				logger.error("retryTask excute failed ,continue..", e);
				RetryTask task = new RetryTask(request);
				timer.newTimeout(task, 5000, TimeUnit.MILLISECONDS);
			}
		}
	}
}

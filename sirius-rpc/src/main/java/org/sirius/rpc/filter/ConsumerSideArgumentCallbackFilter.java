package org.sirius.rpc.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.Maps;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.callback.ArgumentCallbackRequest;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelListener;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

@AutoActive(consumerSide = true)
@Extension(value = "consumerSideArgumentCallback", singleton = false)
public class ConsumerSideArgumentCallbackFilter implements Filter {

	private static final InternalLogger logger = InternalLoggerFactory
			.getInstance(ConsumerSideArgumentCallbackFilter.class);
	public  static final HashedWheelTimer timer = new HashedWheelTimer(
			new DefaultThreadFactory("ConsumerSideArgumentCallbackFilter.timer", true));
	// 缓存 : key : callbackArgument.hashcode -> value : invoker
	private Map<Integer, Invoker> invokers = Maps.newConcurrentMap();
	// 缓存 : key:methodName -> List<ArgumentConfig>
	private Map<String, List<ArgumentConfig>> argumentsMap = Maps.newConcurrentMap();
	// 缓存 : key:channel -> List<ArgumentWarper> 在当前channel关闭后,需要重试的参数集合
	private Map<Channel, List<ArgumentWarper>> retryArguments = Maps.newConcurrentMap();

	private boolean isFirstCall = true;

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		if (isFirstCall) {
			// 此处无需同步控制, 最坏的结果不过是开始时 init()方法多执行几遍
			init(invoker);
			isFirstCall = false;
		}
		String methodName = request.getMethodName();
		List<ArgumentConfig> arguments = argumentsMap.get(methodName);
		if (arguments != null) {
			for (ArgumentConfig argument : arguments) {
				int i = 0;
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
					Invoker callbackInvoker = invokers.get(hashcode);
					if (callbackInvoker == null) {
						// 将callbackArument包装为代理invoker ,以供调用
						callbackInvoker = ProxyFactory.getInvokerNotCache(callbackArument, clazz);
						invokers.putIfAbsent(hashcode, callbackInvoker);
						callbackInvoker = invokers.get(hashcode);
					}

					DefaultInvokeFuture.setCallbackInvoker(hashcode, callbackInvoker);
					argument.setId(String.valueOf(hashcode));
				}
			}
			ArgumentCallbackRequest argRequset = new ArgumentCallbackRequest(request, arguments);
			ArgumentWarper warper = new ArgumentWarper(invoker, argRequset);
			RpcInvokeContent.getContent().set("newArgumentWarper", warper);
			request = argRequset;
		}

		return invoker.invoke(request);
	}

	@Override
	public Response onResponse(Response res , Request request) {
		ArgumentWarper argument = (ArgumentWarper) RpcInvokeContent.getContent().get("newArgumentWarper");
		if (argument != null) {
			Channel channel = (Channel) RpcInvokeContent.getContent().get("channel");
			List<ArgumentWarper> arguments = retryArguments.get(channel);
			if (arguments == null) {
				arguments = new ArrayList<ArgumentWarper>();
				retryArguments.putIfAbsent(channel, arguments);
				arguments = retryArguments.get(channel);
			}
			arguments.add(argument);
			RpcInvokeContent.getContent().remove("newArgumentWarper");
			channel.setListener(new ChannelListener() {
				@Override
				public void onClosed(Channel channel) {
					List<ArgumentWarper> arguments = retryArguments.get(channel);
					for (ArgumentWarper warper : arguments) {
						RetryTask task = new RetryTask(warper);
						timer.newTimeout(task, 1, TimeUnit.MICROSECONDS);
					}
				}
			});
		}
		return res;
	}

	@SuppressWarnings("rawtypes")
	private void init(Invoker invoker) {
		AbstractInvoker _invoker = (AbstractInvoker) invoker;
		ConsumerConfig consumerConfig = (ConsumerConfig) _invoker.getConfig();
		Map<String, MethodConfig> methods = consumerConfig.getMethods();
		if(methods != null) {
			for (MethodConfig method : methods.values()) {
				if (method.getArguments() != null)
					argumentsMap.put(method.getName(), method.getArguments());
			}
		}
	}

	private final class RetryTask implements TimerTask {
		
		private ArgumentWarper warper;
		
		public RetryTask(ArgumentWarper warper) {
			this.warper = warper;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			Invoker invoker = warper.getInvoker();
			ArgumentCallbackRequest request = (ArgumentCallbackRequest) warper.getRequest();
			request.setReconnect(true);
			// 同步调用,保证结果返回;
			RpcInvokeContent.getContent().setInvokeType(RpcConstants.INVOKER_TYPE_SYNC);
			RpcInvokeContent.getContent().setTimeout(3000);
			try {
				invoker.invoke(warper.getRequest());
			} catch (Throwable e) {
				logger.error("retryTask excute failed ,continue.." ,e );
				RetryTask task = new RetryTask(warper);
				timer.newTimeout(task, 5000, TimeUnit.MILLISECONDS);
			}
		}
	}

	private final class ArgumentWarper {
		private Invoker invoker;
		private Request request;

		public ArgumentWarper(Invoker invoker, Request request) {
			this.invoker = invoker;
			this.request = request;
		}

		public Invoker getInvoker() {
			return invoker;
		}

		public void setInvoker(Invoker invoker) {
			this.invoker = invoker;
		}

		public Request getRequest() {
			return request;
		}

		public void setRequest(Request request) {
			this.request = request;
		}
	}
}

package org.sirius.rpc.provider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.sirius.common.util.Maps;
import org.sirius.common.util.ThrowUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.argumentcallback.ArgumentCallbackInvoker;
import org.sirius.rpc.argumentcallback.ArgumentCallbackRequest;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.rpc.executor.disruptor.DisruptorExecutor;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.rpc.server.RpcServer;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultProviderProcessor implements ProviderProcessor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderProcessor.class);
	private InnerExecutor executor;
	private RpcServer server;
	private Map<Integer, Object> proxys = Maps.newConcurrentMap();

	public DefaultProviderProcessor(RpcServer server) {
		this.server = server;
		executor = new DisruptorExecutor(8, null);
	}

	public Invoker lookupInvoker(Request request) {
		return server.lookupInvoker(request);
	}

	@Override
	public void handlerRequest(Channel channel, Request request) {
		if (logger.isDebugEnabled()) {
			logger.debug("the requestID is {}, the request interface is {}," + " method is {}, params is {}",
					request.invokeId(), request.getClassName(), request.getMethodName(), request.getParameters());
		}
		try {
			handleArgumentCallback(channel, request);
			Invoker invoker = server.lookupInvoker(request);
			if (invoker == null) {
				throw new RpcException("the invoker for interface " + request.getClassName() + " is not founded,");
			}
			RpcInvokeContent content = RpcInvokeContent.getContent();
			content.setProviderSide(true);
			executor.execute(new RequestTask(this, invoker, channel, request));
		} catch (Throwable t) {
			handlerException(channel, request, t);
		}
	}

	private void handleArgumentCallback(Channel channel, Request request) throws Throwable {
		if (request instanceof ArgumentCallbackRequest) {
			ArgumentCallbackRequest callbackRequest = (ArgumentCallbackRequest) request;
			List<ArgumentConfig> arguments = callbackRequest.getArguments();
			for (ArgumentConfig argument : arguments) {
				Integer id = Integer.valueOf(argument.getId());
				// 如果是同一个参数回调对象就不能重复创建
				Object proxy = proxys.get(id);
				if (proxy == null) {
					int index = argument.getIndex();
					Object callbackObject = request.getParameters()[index];
					Class<?> clazz = request.getParametersType()[index];
					Class<?>[] interfaces = callbackObject.getClass().getInterfaces();
					ArgumentCallbackInvoker callbackInvoker = new ArgumentCallbackInvoker(channel, Integer.valueOf(argument.getId()),
							argument.getRetry(), argument.getAttempts(), argument.getDelay());
					proxy = ProxyFactory.getProxyNotCache(callbackInvoker, interfaces);
					proxys.putIfAbsent(id, proxy);
					// 将参数替换为callback代理
					request.getParameters()[index] = proxy;
				} else {
					// proxy不为null,表明客户端因为网络闪断又重新执行一次注册方法,原有的channel不能用了,需要替换为新的可用channel
					if (callbackRequest.isReconnect()) {
						Class<?> clazz = proxy.getClass();
						Field invokerFiled = clazz.getDeclaredField("invoker");
						invokerFiled.setAccessible(true);
						ArgumentCallbackInvoker callbackInvoker = (ArgumentCallbackInvoker) invokerFiled.get(proxy);
						callbackInvoker.swapChannel(channel);
						// 执行到这里就返回, 将channel替换好就行了,不会再重复执行一遍调用方法
						Response response = new Response(request.invokeId());
						response.setSerializerCode(request.getSerializerCode());
					}
				}
			}
		}
	}

	@Override
	public void handlerResponse(Channel channel, Response response) {
		try {
			executor.execute(new ResponseTask(channel, response));
		} catch (Throwable t) {
			ThrowUtil.throwException(t);
		}

	}

	@Override
	public void handlerException(Channel channel, Request request, Throwable t) {
		// logger.error("the request of {} processing failed ,the reasons maybe {}",
		// request.invokeId(), t);
		// Response response = new Response(request.invokeId());
		// response.setSerializerCode(request.getSerializerCode());
		// response.setResult(t);
		// try {
		// channel.send(response);
		// } catch (Exception e) {
		// logger.error("the response of {} sended failed,the reasons maybe {}",
		// request.invokeId(), e);
		// }
		ThrowUtil.throwException(t);
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

}

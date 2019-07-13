package org.sirius.rpc.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.Maps;
import org.sirius.rpc.Filter;
import org.sirius.rpc.callback.ArgumentCallbackRequest;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

@AutoActive(consumerSide = true)
@Extension(value = "consumerSideArgumentCallback", singleton = false)
public class ConsumerSideArgumentCallbackFilter implements Filter {

	// 缓存 : key : callbackArgument -> value : invoker
	private Map<Object, Invoker> invokers = Maps.newConcurrentMap();

	// 缓存 : key:methodName -> List<ArgumentConfig>
	private Map<String, List<ArgumentConfig>> argumentsMap = Maps.newConcurrentMap();
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
					Invoker callbackInvoker = invokers.get(callbackArument);
					if (callbackInvoker == null) {
						//将callbackArument包装为代理invoker ,以供调用
						callbackInvoker = ProxyFactory.getInvoker(callbackArument, clazz);
						invokers.putIfAbsent(callbackArument, callbackInvoker);
						callbackInvoker = invokers.get(callbackArument);
					}
					DefaultInvokeFuture.setCallbackInvoker(request.invokeId(), callbackInvoker);
				}
			}
			ArgumentCallbackRequest argRequset = new ArgumentCallbackRequest(request, arguments);
			request = argRequset;
		}

		return invoker.invoke(request);
	}

	@SuppressWarnings("rawtypes")
	private void init(Invoker invoker) {
		AbstractInvoker _invoker = (AbstractInvoker) invoker;
		ConsumerConfig consumerConfig = (ConsumerConfig) _invoker.getConfig();
		Map<String, MethodConfig> methods = consumerConfig.getMethods();
		for (MethodConfig method : methods.values()) {
			if(method.getArguments() != null)
			argumentsMap.put(method.getName(), method.getArguments());
		}
	}
}

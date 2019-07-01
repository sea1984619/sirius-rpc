package org.sirius.rpc.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.Maps;
import org.sirius.config.ArgumentConfig;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.rpc.Filter;
import org.sirius.rpc.Invoker;
import org.sirius.rpc.callback.ArgumentCallbackRequest;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class CallBackFilter implements Filter {

	private ConsumerConfig consumerConfig;
	private Map<String, MethodConfig> methods;
	private Map<Object, Invoker> invokers = Maps.newConcurrentMap();

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}
	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
		methods = consumerConfig.getMethods();
	}
	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		String methodName = request.getMethodName();
		MethodConfig method = methods.get(methodName);
		List<ArgumentConfig> arguments;
		if((arguments = method.getArguments()) != null) {
			List<ArgumentConfig> newList = new ArrayList<>();
			for(ArgumentConfig argument : arguments) {
				if(argument.isCallback()) {
					int index = argument.getIndex();
					if(index < 0 || index > request.getParameters().length - 1) {
						throw new IndexOutOfBoundsException("index in <argument> is out Of bound");
					}
					Object callback = request.getParameters()[index];
					Class clazz = callback.getClass();
					if(ClassUtil.isPrimitive(clazz) || clazz.isArray() || clazz.isAssignableFrom(Collection.class)) {
						throw new IllegalStateException("the callback argument type is " + clazz + ","
								                          + " must not be a Array or a Collection or a primitive type" );
					}
					newList.add(argument);
					Invoker callbackInvoker = invokers.get(callback);
					if(callbackInvoker == null) {
						callbackInvoker = ProxyFactory.getInvoker(callback, clazz);
						invokers.putIfAbsent(callback ,callbackInvoker);
					}
					callbackInvoker = invokers.get(callback);
					ResultFutureContent.setCallbackInvoker(request.invokeId(), callbackInvoker);
				}
			}
			if(!newList.isEmpty()) {
				ArgumentCallbackRequest argRequset = new ArgumentCallbackRequest(request, newList);
				request = argRequset;
			}
		}
		
		return invoker.invoke(request);
	}

}

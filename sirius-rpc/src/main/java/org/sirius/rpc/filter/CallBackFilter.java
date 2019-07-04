package org.sirius.rpc.filter;

import java.util.ArrayList;
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
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

@Extension(value = "callback")
@AutoActive(consumerSide = true)
public class CallBackFilter implements Filter {

	private Map<Object, Invoker> invokers = Maps.newConcurrentMap();

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		System.out.println("callback调用.....");
		AbstractInvoker _invoker = (AbstractInvoker) invoker;
		ConsumerConfig consumerConfig = (ConsumerConfig) _invoker.getConfig();
		Map<String, MethodConfig> methods =  consumerConfig.getMethods();
		String methodName = request.getMethodName();
		MethodConfig method = methods.get(methodName);
		List<ArgumentConfig> arguments = method.getArguments();
		if(arguments != null) {
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

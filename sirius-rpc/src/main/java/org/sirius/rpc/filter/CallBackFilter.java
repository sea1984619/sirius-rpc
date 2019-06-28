package org.sirius.rpc.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sirius.common.util.ClassUtil;
import org.sirius.config.ArgumentConfig;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.rpc.Filter;
import org.sirius.rpc.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class CallBackFilter implements Filter {

	private ConsumerConfig consumerConfig;
	private Map<String, MethodConfig> methods;
	
	
	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		String methodName = request.getMethodName();
		MethodConfig method = methods.get(methodName);
		List<ArgumentConfig> arguments;
		if((arguments = method.getArguments()) != null) {
			for(ArgumentConfig argument : arguments) {
				if(argument.isCallback()) {
					int index = argument.getIndex();
					if(index < 0 || index > request.getParameters().length - 1) {
						throw new IndexOutOfBoundsException("index in <argument> is out Of bound");
					}
					Object callback = request.getParameters()[index];
					Class clazz = callback.getClass();
					if(ClassUtil.isPrimitive(clazz) || clazz.isArray() || clazz.isAssignableFrom(Collection.class)) {
						throw new IllegalStateException("the callback argument type is " + clazz + 
								                           ", must not be a Array or a Collection or a primitive type" );
					}
					Invoker callbackInvoker = ProxyFactory.getInvoker(callback, clazz);
				}
			}
		}
		return null;
	}

}

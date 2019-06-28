package org.sirius.rpc.consumer.invoke;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sirius.config.ArgumentConfig;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker implements Invoker{

	private ConsumerConfig consumerConfig;
	private Map<String, MethodConfig> methods;
	
	public ConsumerProxyInvoker(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
		methods = consumerConfig.getMethods();
		
	}

	@Override
	public Response invoke(Request request) throws Throwable {
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
					Class<?> clazz = callback.getClass();
					if(isPrimitive(clazz) || clazz.isArray() || clazz.isAssignableFrom(Collection.class)) {
						throw new IllegalStateException("the callback argument type is " + clazz + 
								                           ", must not be a Array or a Collection or a primitive type" );
					}
					
				}
			}
		}
	    
		
		
		return null;
	}
	
	private static boolean isPrimitive(Class<?> cls) {
		return cls.isPrimitive() || cls == Boolean.class || cls == Byte.class || cls == Character.class
				|| cls == Short.class || cls == Integer.class || cls == Long.class || cls == Float.class
				|| cls == Double.class || cls == String.class || cls == Date.class || cls == Class.class;
	}
}

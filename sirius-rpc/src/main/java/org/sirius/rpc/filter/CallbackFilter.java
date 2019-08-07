package org.sirius.rpc.filter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.ReflectUtils;
import org.sirius.common.util.ThrowUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

import com.google.common.collect.Maps;

@AutoActive(consumerSide = true)
@Extension(value = "callbackFilter", singleton = true)
public class CallbackFilter implements Filter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(CallbackFilter.class);
	// key-> methodName : value->warper map
	private Map<String, Map<String, CallbackWarper>> callbackMap = Maps.newConcurrentMap();
	private final static String ONINVOKE = "oninvoke";
	private final static String ONRETURN = "onreturn";
	private boolean isFirstCall = true;

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {

		if (isFirstCall) {
			// 此处无需同步控制, 最坏的结果不过是开始时 init()方法多执行几遍
			init(invoker);
			isFirstCall = false;
		}
		if (hasInvokeCallback(invoker, request)) {
			CallbackWarper warper = callbackMap.get(request.getMethodName()).get(ONINVOKE);
			warper.getMethod().invoke(warper.getObject(), request.getParameters());
		}

		return invoker.invoke(request);
	}

	private boolean hasInvokeCallback(Invoker invoker, Request request) {
		if (!callbackMap.containsKey(request.getMethodName()))
			return false;
		Map<String, CallbackWarper> methodMap = callbackMap.get(request.getMethodName());
		return methodMap.containsKey(ONINVOKE);
	}

	private void init(Invoker invoker) {
		AbstractInvoker<?> _invoker = (AbstractInvoker<?>) invoker;
		ConsumerConfig<?> consumerConfig = (ConsumerConfig<?>) _invoker.getConfig();
		Map<String, MethodConfig> methodConfigMap = consumerConfig.getMethods();
		if (methodConfigMap != null) {
			Class<?> clazz = consumerConfig.getProxyClass();
			for (Entry<String, MethodConfig> entry : methodConfigMap.entrySet()) {
				MethodConfig methodConfig = entry.getValue();
				Map<String, CallbackWarper> methodMap = callbackMap.get(methodConfig.getName());
				if (methodMap == null) {
					methodMap = Maps.newConcurrentMap();
					callbackMap.putIfAbsent(methodConfig.getName(), methodMap);
					methodMap = callbackMap.get(methodConfig.getName());
				}
				Method[] methods = clazz.getDeclaredMethods();
				// 需要回调方法的 方法..
				Method originMethod = null;
				for (Method method : methods) {
					if (method.getName().equals(entry.getKey())) {
						// 默认服务接口没有同名的重载方法, 因为无法做到同名重载方法的methodconfig配置
						originMethod = method;
						return;
					}
				}
				if (methodConfig.getOninvoke() != null) {
					Object object = methodConfig.getOninvoke();
					String methodName = methodConfig.getOninvokeMethod();
					try {
						Method callbackMethod = object.getClass().getDeclaredMethod(methodName,originMethod.getParameterTypes());
						callbackMethod.setAccessible(true);
						CallbackWarper warper = new CallbackWarper(callbackMethod, object);
						methodMap.putIfAbsent(ONINVOKE, warper);
					} catch (NoSuchMethodException e) {
						logger.error(
								"the oninvoke callback method in class {} is not found ,please check the callback method name "
										+ "and the parameterTypes of the callback method must be the same as the invoke method",
								object.getClass().toString(), e);
						ThrowUtil.throwException(new RpcException("creat oninvoke method failed"));
					}
				}
				if (methodConfig.getOnreturn() != null) {
					Object object = methodConfig.getOnreturn();
					String methodName = methodConfig.getOnreturnMethod();

					Class<?>[] paramTypes = originMethod.getParameterTypes();
					Class<?> returnType = originMethod.getReturnType();
					Class<?>[] types = new Class<?>[paramTypes.length + 1];
					types[0] = returnType;
					System.arraycopy(paramTypes, 0, types, 1, paramTypes.length);
					Method returnMethod = null;
					try {
						returnMethod = object.getClass().getDeclaredMethod(methodName, types);
					} catch (NoSuchMethodException e) {
						try {
							Class<?>[] temType = new Class<?>[] {returnType};
							returnMethod = object.getClass().getDeclaredMethod(methodName, temType);
						} catch (NoSuchMethodException ee) {
							logger.error(
									"the onreturn callback method in class {} is not found ,please check the callback method name "
											+ "and the parameterTypes of the return method must match the invoke method or only have the result type",
									object.getClass().toString(), e);
							ThrowUtil.throwException(new RpcException("creat onreturn method failed"));
						}

						returnMethod.setAccessible(true);
						CallbackWarper warper = new CallbackWarper(returnMethod, object);
						methodMap.putIfAbsent(ONRETURN, warper);
					}
				}
			}
		}
	}

	@Override
	public Response onResponse(Response res , Request request) {
		if(hasReturnCallback()) {
			
		}
		return res;
	}
	private boolean hasReturnCallback() {
		// TODO Auto-generated method stub
		return false;
	}
	private class CallbackWarper {

		private Method method;
		private Object object;

		public CallbackWarper(Method method, Object object) {
			this.method = method;
			this.object = object;
		}

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}
	}
}

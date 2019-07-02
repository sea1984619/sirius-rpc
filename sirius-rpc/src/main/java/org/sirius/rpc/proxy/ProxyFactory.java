package org.sirius.rpc.proxy;

import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.bytecode.ProxyGenerator;
import org.sirius.rpc.proxy.bytecode.Wrapper;

public class ProxyFactory {
	
	
	public static Object getProxy(Invoker invoker, Class<?>... ics) {
		return ProxyGenerator.getProxy(invoker, ics);
	}
	
	public static <T> Invoker getInvoker(T providerImpl, Class<T> provideClass) {

		Wrapper wrapper = Wrapper.getWrapper(provideClass.getClass().getName().indexOf('$') < 0 ? providerImpl.getClass() : provideClass);
		return new ProviderProxyInvoker<T>(providerImpl, provideClass) {
			@Override
			public Object doInvoke(T provider, String mn, Class<?>[] types, Object[] args) throws Throwable {
				return wrapper.invokeMethod(provider, mn, types, args);
			}
		};
	}
}

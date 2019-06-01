package org.sirius.rpc.proxy;


import org.sirius.rpc.Invoker;
import org.sirius.rpc.provider.Test;
import org.sirius.rpc.provider.TestImpl;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.bytecode.Wrapper;

public class ProviderProxyUtil {

	public static  <T> Invoker getInvoker(T providerImpl,Class<T> provideClass) {
		
		Wrapper wrapper = Wrapper.getWrapper(provideClass.getClass().getName().indexOf('$') < 0 ? providerImpl.getClass() : provideClass);
		
		return new ProviderProxyInvoker<T>(providerImpl,provideClass) {

			@Override
			public Object doInvoke(T provider, String mn, Class<?>[] types, Object[] args) throws Throwable {
				return wrapper.invokeMethod(provider, mn, types, args);
			}
		};
	}
	
	public static void main(String args[]) {
		TestImpl im = new TestImpl();
		
		ProviderProxyUtil.getInvoker(im, Test.class);
	}
}

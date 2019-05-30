package org.sirius.rpc.proxy;

import org.sirius.rpc.AbstractInvoker;
import org.sirius.rpc.Invoker;
import org.sirius.rpc.proxy.bytecode.ProxyUtil;
import org.sirius.rpc.proxy.bytecode.Wrapper;

public class JavassistProxyFactory implements ProxyFactory{



	@Override
	public Object getProxy(Invoker invoker, Class[] interfaces) throws Exception {
		return ProxyUtil.getProxy(invoker, interfaces);
	}

	@Override
	public Invoker getInvoker(Object proxy, Class type) {
		Wrapper wrapper = Wrapper.getWrapper(type);
		return new AbstractInvoker() {
			@Override
			public Object doInvoke(Object t,String methodName, Class<?>[] argsType, Object[] args) throws Exception {
				return  wrapper.invokeMethod(t, methodName, argsType, args);
			}
		};
	}

}

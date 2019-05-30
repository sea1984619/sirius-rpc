package org.sirius.rpc.proxy;

import org.sirius.rpc.Invoker;

public interface ProxyFactory<T> {

	/*
	 * 创建客户端代理
	 */
	public Object getProxy(Invoker<T> invoker , Class<?>[] interfaces) throws Exception;
	
	/*
	 * 创建服务端invoker
	 */
	public Invoker<T> getInvoker(T proxy, Class<T> type);
	
}

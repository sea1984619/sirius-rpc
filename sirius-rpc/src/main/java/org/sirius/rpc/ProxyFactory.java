package org.sirius.rpc;

public interface ProxyFactory<T> {

	/*
	 * 创建客户端代理
	 */
	public Object getProxy(Invoker<T> invoker , Class<?> clazz) throws Exception;
	
	/*
	 * 创建服务端invoker
	 */
	public Invoker<T> getInvoker(Class<T> t);
	
}

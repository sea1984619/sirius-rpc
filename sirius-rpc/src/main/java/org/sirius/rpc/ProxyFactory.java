package org.sirius.rpc;

public interface ProxyFactory {

	/*
	 * 创建客户端代理
	 */
	public Object getProxy(Invoker invoker , Class clazz) throws Exception;
}

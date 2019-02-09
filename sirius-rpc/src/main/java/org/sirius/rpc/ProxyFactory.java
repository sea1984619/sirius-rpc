package org.sirius.rpc;

public interface ProxyFactory {

	/*
	 * 创建客户端代理
	 */
	public <T> T getProxy(Invoker invoker , Class<T> t) throws Exception;
}

package org.sirius.rpc;

import org.sirius.rpc.consumer.invoker.Invoker;

public interface ProxyFactory {

	/*
	 * 创建客户端代理
	 */
	public Object getProxy(Invoker invoker , Class clazz) throws Exception;
	
	
}

package org.sirius.rpc;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sirius.common.util.Maps;
import org.sirius.common.util.internal.InternalThreadLocal;

/*
 * rpc调用上下文, 供使用者进行一些调用参数配置, 以及获取异步调用的future;
 */
public class RpcInvokeContent {

	private static InternalThreadLocal<RpcInvokeContent> Local = new InternalThreadLocal<RpcInvokeContent>() {
		@Override
		protected RpcInvokeContent initialValue() {
			return new RpcInvokeContent();
		}
	};

	private int timeout;
	private Map values = Maps.newHashMap();
	private Future future;
	
	public static RpcInvokeContent getContent() {
		return Local.get();
	}

	public static void setLocalContent(RpcInvokeContent content) {
		Local.set(content);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public void set(Object key, Object value) {
		values.put(key, value);
	}

	public Object get(Object key) throws InterruptedException, ExecutionException {
		return values.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> Future<T> getFuture() {
		return (Future<T>) future;
	}
	public void setFuture(Future<?> future) {
		this.future = future;
	}
}

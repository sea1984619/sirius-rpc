package org.sirius.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sirius.common.util.Maps;
import org.sirius.common.util.internal.InternalThreadLocal;

/*
 * 保存上一次调用的结果 
 */
public class RpcContent {

	private static InternalThreadLocal<RpcContent> Local = new InternalThreadLocal<RpcContent>() {
		@Override
		protected RpcContent initialValue() {
			return new RpcContent();
		}
	};

	private Map values = Maps.newHashMap();
	private Future future;
	
	public static RpcContent getContent() {
		return Local.get();
	}

	public static void setLocalContent(RpcContent content) {
		Local.set(content);
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

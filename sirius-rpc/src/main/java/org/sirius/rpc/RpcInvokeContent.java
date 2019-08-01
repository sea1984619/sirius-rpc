package org.sirius.rpc;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sirius.common.util.Maps;
import org.sirius.common.util.internal.InternalThreadLocal;

/*
 * rpc调用上下文, 供使用者进行一些调用参数配置, 以及获取异步调用的future;
 * rpc调用存在 A调用B,而B又调用C的情况 ,此时B的content会发生转变,从服务端转换为客户端;
 */
public class RpcInvokeContent {

	private static InternalThreadLocal<RpcInvokeContent> local = new InternalThreadLocal<RpcInvokeContent>() {
		@Override
		protected RpcInvokeContent initialValue() {
			return new RpcInvokeContent();
		}
	};

	//存储状态变化时的 服务端content;
	private static InternalThreadLocal<RpcInvokeContent> backup = new InternalThreadLocal<RpcInvokeContent>() {
		@Override
		protected RpcInvokeContent initialValue() {
			return new RpcInvokeContent();
		}
	};
	private int timeout;
	private String invokeType;
	private boolean providerSide;

	private Map values = Maps.newHashMap();
	private Future future;

	public static void swapContent() {
		InternalThreadLocal<RpcInvokeContent> tem ;
		tem = local;
		local = backup;
		backup = tem;
	}
	
	public static RpcInvokeContent getBackupContent() {
		return backup.get();
	}

	public static RpcInvokeContent getContent() {
		return local.get();
	}

	public static void setContent(RpcInvokeContent content) {
		local.set(content);
	}

	public String getInvokeType() {
		return invokeType;
	}

	public void setInvokeType(String invokeType) {
		this.invokeType = invokeType;
	}
	
	public boolean isProviderSide() {
		return providerSide;
	}

	public void setProviderSide(boolean providerSide) {
		this.providerSide = providerSide;
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

	public Object get(Object key) {
		return values.get(key);
	}

	public void remove(Object key) {
		values.remove(key);
	}

	@SuppressWarnings("unchecked")
	public <T> Future<T> getFuture() {
		return (Future<T>) future;
	}

	public void setFuture(Future<?> future) {
		this.future = future;
	}
}

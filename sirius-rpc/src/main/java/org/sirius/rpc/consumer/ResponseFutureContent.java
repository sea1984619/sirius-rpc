package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.invoker.Invoker;

public class ResponseFutureContent {

	@SuppressWarnings("rawtypes")
	//{key -> invokeId  : value -> 对应的future}
	private static ConcurrentMap<Long,ResponseFuture> futureContent = Maps.newConcurrentMap();
	
	//参数回调invoker map   {key ->invokeId : value ->回调参数对象生成的invoker} 
	private static ConcurrentMap<Long,Invoker> callbackInvokers = Maps.newConcurrentMap();
	
	public static void add(Long invokerId ,ResponseFuture future) {
		futureContent.putIfAbsent(invokerId, future);
	}
	
	public static CompletableFuture get(Long invokerId) {
		return futureContent.get(invokerId);
	}
	
	public static void setCallbackInvoker(Long invokerId,Invoker invoker) {
		callbackInvokers.putIfAbsent(invokerId, invoker);
	}
	
	public static Invoker getCallbackInvoker(Long invokerId) {
		return callbackInvokers.get(invokerId);
	}
}

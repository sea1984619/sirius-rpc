package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.invoker.Invoker;

public class ResultFutureContent {

	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Long,CompletableFuture> futureContent = Maps.newConcurrentMap();
	private static ConcurrentMap<Long,Invoker> callbackInvokers = Maps.newConcurrentMap();
	
	public static void add(Long invokerId ,CompletableFuture future) {
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

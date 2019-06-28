package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.Invoker;

public class ResultFutureContent {

	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Long,CompletableFuture> futureContent = Maps.newConcurrentMap();
	private static ConcurrentMap<String,Invoker> callbackInvokers = Maps.newConcurrentMap();
	
	public static void add(Long invokerId ,CompletableFuture future) {
		futureContent.putIfAbsent(invokerId, future);
	}
	
	public static CompletableFuture get(Long invokerId) {
		return futureContent.get(invokerId);
	}
	
	public static void setCallbackInvoker(String className,Invoker invoker) {
		callbackInvokers.putIfAbsent(className, invoker);
	}
	
	public static Invoker getCallbackInvoker(String className) {
		return callbackInvokers.get(className);
	}
}

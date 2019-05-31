package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;

public class ResultFutureContent {

	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Long,CompletableFuture> futureContent = Maps.newConcurrentMap();
	
	public static void add(Long invokerId ,CompletableFuture future) {
		futureContent.putIfAbsent(invokerId, future);
	}
	public static CompletableFuture get(Long invokerId) {
		return futureContent.get(invokerId);
	}
}

package org.sirius.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.common.util.internal.InternalThreadLocal;

/*
 * 保存上一次调用的结果 
 */
public class RpcContent {

	private static InternalThreadLocal<CompletableFuture<Object>> content = new InternalThreadLocal<CompletableFuture<Object>>() {
		 @Override
	        protected CompletableFuture<Object> initialValue() {
	            return new CompletableFuture<Object>();
	        }
	};
	
	public static void add(CompletableFuture<Object> future) {
		content.set(future);
	}
	public  static CompletableFuture<Object> get() throws InterruptedException, ExecutionException {
		return content.get();
	}
}

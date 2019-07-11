package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;

/*
 * 直接继承CompletableFuture,因为CompletableFuture的功能非常完备,强大
 */
public class ResponseFuture<Response> extends CompletableFuture<Response> {

	public volatile boolean isFilted;
	
	
	
}

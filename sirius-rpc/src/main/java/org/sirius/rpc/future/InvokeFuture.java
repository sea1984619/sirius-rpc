package org.sirius.rpc.future;

import java.util.concurrent.CompletionStage;

import org.sirius.transport.api.Response;

public interface InvokeFuture<V> extends CompletionStage<V>{

	Object getResult() throws Throwable;
	
	Response getResponse() throws Throwable;
}

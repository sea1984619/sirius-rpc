package org.sirius.rpc.future;

import java.util.concurrent.CompletionStage;

public interface InvokeFuture<V> extends CompletionStage<V>{

	Object getResult();
}

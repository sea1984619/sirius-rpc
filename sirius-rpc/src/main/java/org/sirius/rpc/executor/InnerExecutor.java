package org.sirius.rpc.executor;

public interface InnerExecutor {

	 void execute(Runnable task);
	 
	 void close();
}

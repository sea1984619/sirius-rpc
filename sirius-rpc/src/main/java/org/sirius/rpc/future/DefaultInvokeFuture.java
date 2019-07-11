package org.sirius.rpc.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.common.util.ThrowUtil;
import org.sirius.transport.api.Response;

public class DefaultInvokeFuture<V> extends CompletableFuture<V> implements InvokeFuture<V> {

	public volatile boolean isFilted;

	@Override
	public Object getResult() {
		Response response = null;
		synchronized (this) {
			while (!isFilted) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					ThrowUtil.throwException(e);
				}
			}
		}

		try {
			response = (Response) super.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return response.getResult();
	}

}

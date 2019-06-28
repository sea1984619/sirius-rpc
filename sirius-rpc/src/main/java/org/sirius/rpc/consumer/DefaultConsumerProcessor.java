package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.Invoker;
import org.sirius.rpc.callback.ArgumentCallbackResponse;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultConsumerProcessor implements ConsumerProcessor {

	@Override
	public void handleResponse(Channel channel, Response response) {
		if(response instanceof ArgumentCallbackResponse) {
			handleArgumentCallbackResponse(response);
		}
		Object result = response.getResult();
		CompletableFuture<Object> future = ResultFutureContent.get(response.invokeId());
		if(future != null) {
			future.complete(result);
		}
	}

	private void handleArgumentCallbackResponse(Response response) {
		ArgumentCallbackResponse argResponse = (ArgumentCallbackResponse) response;
		Request request = (Request) argResponse.getResult();
		String className = request.getClassName();
		Invoker invoker = ResultFutureContent.getCallbackInvoker(className);
		try {
			invoker.invoke(request);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		
	}

}

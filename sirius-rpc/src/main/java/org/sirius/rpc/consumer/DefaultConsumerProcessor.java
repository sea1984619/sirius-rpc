package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;

import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultConsumerProcessor implements ConsumerProcessor {

	@Override
	public void handleResponse(Channel channel, Response response) {
		Object result = response.getResult();
		CompletableFuture<Object> future = ResultFutureContent.get(response.invokeId());
		if(future != null) {
			future.complete(result);
		}
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}

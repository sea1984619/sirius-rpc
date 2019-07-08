package org.sirius.rpc.consumer;

import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.callback.ArgumentCallbackResponse;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultConsumerProcessor implements ConsumerProcessor {

	@Override
	public void handleResponse(Channel channel, Response response) {

		if (response instanceof ArgumentCallbackResponse) {
			handleArgumentCallbackResponse(channel, response);
		}

		Object result = response.getResult();
		CompletableFuture<Object> future = ResultFutureContent.get(response.invokeId());
		if (future != null) {
			future.complete(result);
		}
	}

	private void handleArgumentCallbackResponse(Channel channel, Response response) {
		ArgumentCallbackResponse argResponse = (ArgumentCallbackResponse) response;
		// 获取回调参数
		Request request = (Request) argResponse.getResult();
		long invokeId = response.invokeId();
		Invoker invoker = ResultFutureContent.getCallbackInvoker(invokeId);
		try {
			//暂时不考虑返回结果,后续再加入
			invoker.invoke(request);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {

	}

}

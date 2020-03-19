package org.sirius.rpc.consumer;


import org.sirius.common.util.ThrowUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.argumentcallback.ArgumentCallbackResponse;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.provider.DefaultProviderProcessor;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultConsumerProcessor implements ConsumerProcessor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderProcessor.class);

	@Override
	public void handleResponse(Channel channel, Response response) {

		if (response instanceof ArgumentCallbackResponse) {
			handleArgumentCallbackResponse(channel, response);
		}else {
			 DefaultInvokeFuture.received(response);
		}
	}

	private void handleArgumentCallbackResponse(Channel channel, Response response) {
		ArgumentCallbackResponse argResponse = (ArgumentCallbackResponse) response;
		// 获取回调参数
		Request request = (Request) argResponse.getResult();
		Long callbackId = argResponse.invokeId();
		Invoker<?> invoker = ArgumentCallbackHandler.getCallbackInvoker(callbackId.intValue());
		try {
			Response _response = invoker.invoke(request);
			channel.send(_response);
		} catch (Throwable e) {
			logger.error(e);
			ThrowUtil.throwException(e);
		}
	}

	@Override
	public void shutdown() {

	}
}

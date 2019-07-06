package org.sirius.rpc.provider;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.rpc.executor.disruptor.DisruptorExecutor;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.server.RpcServer;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class DefaultProviderProcessor implements ProviderProcessor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderProcessor.class);
	private InnerExecutor executor;
	private RpcServer server;

	public DefaultProviderProcessor(RpcServer server) {
		this.server = server;
		executor = new DisruptorExecutor(8, null);
	}

	public Invoker lookupInvoker(Request request) {
		return server.lookupInvoker(request);
	}

	@Override
	public void handlerRequest(Channel channel, Request request) {
		if (logger.isDebugEnabled()) {
			logger.debug("the requestID is {}, the request interface is {}," + " method is {}, params is {}",
					request.invokeId(), request.getClassName(), request.getMethodName(), request.getParameters());
		}
		try {
			Invoker invoker = server.lookupInvoker(request);
			if (invoker == null) {
				throw new RpcException("the invoker for interface " + request.getClassName() + " is not founded,");
			}
			executor.execute(new RequestTask(this, invoker, channel, request));
		} catch (Throwable t) {
			handlerException(channel, request, t);
		}
	}

	@Override
	public void handlerException(Channel channel, Request request, Throwable t) {
		logger.error("the request of {} processing failed ,the reasons maybe {}", request.invokeId(), t);
		Response response = new Response(request.invokeId());
		response.setSerializerCode(request.getSerializerCode());
		response.setResult(t);
		try {
			channel.send(response);
		} catch (Exception e) {
			logger.error("the response of {} sended failed,the reasons maybe {}", request.invokeId(), e);
		}
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

}

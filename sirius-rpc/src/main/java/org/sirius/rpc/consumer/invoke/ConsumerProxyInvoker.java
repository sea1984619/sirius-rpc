package org.sirius.rpc.consumer.invoke;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.consumer.cluster.Cluster;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.provider.DefaultProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker extends AbstractInvoker {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderProcessor.class);
	private Cluster cluster;
	private ConsumerConfig consumerConfig;

	public ConsumerProxyInvoker(ConsumerConfig consumerConfig) {
		super(consumerConfig);
		consumerConfig = (ConsumerConfig) getConfig();
		cluster = new Cluster();
		cluster.setConsumerConfig(consumerConfig);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {

		Response response = null;
		if (!consumerConfig.isGeneric()) {
            // 找到调用类型， generic的时候类型在filter里进行判断
            request.setInvokeType(consumerConfig.getMethodInvokeType(request.getMethodName()));
        }
		try {
			response = cluster.invoke(request);
		} catch (Throwable t) {
			logger.error("invocation of {} failed, the reason maybe {}",
					request.getClassName() + request.getMethodName(), t.getCause());
			response = new Response(request.invokeId());
			response.setResult(t);
		}
		return response;
	}
}

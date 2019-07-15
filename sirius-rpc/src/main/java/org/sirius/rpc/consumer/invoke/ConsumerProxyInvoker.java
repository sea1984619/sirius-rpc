package org.sirius.rpc.consumer.invoke;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.consumer.cluster.AbstractCluster;
import org.sirius.rpc.consumer.cluster.Cluster;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.provider.DefaultProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker<T> extends AbstractInvoker<T> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerProxyInvoker.class);
	private Cluster cluster;
	private ConsumerConfig<T> consumerConfig;

	public ConsumerProxyInvoker(ConsumerConfig<T> consumerConfig) {
		super(consumerConfig);
		this.consumerConfig = (ConsumerConfig<T>) getConfig();
		cluster = new AbstractCluster();
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
			logger.error("invocation of {} invoked failed, the reason maybe {}",
					request.getClassName() + request.getMethodName(), t);
			throw t;
		}
		return response;
	}
}

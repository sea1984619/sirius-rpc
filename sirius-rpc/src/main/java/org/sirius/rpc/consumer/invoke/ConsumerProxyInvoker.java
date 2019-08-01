package org.sirius.rpc.consumer.invoke;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.client.RpcClient;
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
	private RpcClient client;

	@SuppressWarnings("unchecked")
	public ConsumerProxyInvoker(ConsumerConfig<T> consumerConfig, RpcClient client) {
		super(consumerConfig);
		this.consumerConfig = (ConsumerConfig<T>) getConfig();
		this.client = client;
		cluster = new AbstractCluster<T>(consumerConfig, client);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {

		Response response = null;
		if (!consumerConfig.isGeneric()) {
			// 找到调用类型， generic的时候类型在filter里进行判断
			String invokeType = RpcInvokeContent.getContent().getInvokeType();
			if (invokeType != null) {
				request.setInvokeType(invokeType);
				// 调用一次就作废
				RpcInvokeContent.getContent().setInvokeType(null);
			} else {
				request.setInvokeType(consumerConfig.getMethodInvokeType(request.getMethodName()));
			}
		}

		int timeout = RpcInvokeContent.getContent().getTimeout();
		if (timeout != 0) {
			request.setTimeout(timeout);
			// 调用一次就作废
			RpcInvokeContent.getContent().setTimeout(0);
		} else {
			timeout = consumerConfig.getMethodTimeout(request.getMethodName());
			request.setTimeout(timeout);
		}

		response = cluster.invoke(request);
		return response;
	}
}

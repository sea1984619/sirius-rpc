package org.sirius.rpc.consumer.invoke;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.cluster.AbstractCluster;
import org.sirius.rpc.consumer.cluster.Cluster;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerInvoker<T> extends AbstractInvoker<T> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerProxyInvoker.class);
	private Cluster cluster;
	private ConsumerConfig<T> consumerConfig;
	private RpcClient client;
	@SuppressWarnings("unchecked")
	public ConsumerInvoker(ConsumerConfig<T> consumerConfig, RpcClient client) {
		super(consumerConfig);
		this.consumerConfig = (ConsumerConfig<T>) getConfig();
		this.client = client;
		cluster = new AbstractCluster<T>(consumerConfig, client);
	}

	@Override
	public Response invoke(Request request) throws Throwable {
		
		RpcInvokeContent content = RpcInvokeContent.getContent();
		boolean needSwapWhenReturn = false;
		 //这是一个consumer端的invoker, 如果 content.isProviderSide() 为true ,表明 这个一个 A 调用 B服务,
		 //而B服务 作为调用者,又调用C的情况,此时content 应从服务端 转换为客户端;
		if(content.isProviderSide()) {
			RpcInvokeContent.swapContent();
			needSwapWhenReturn = true;
		}
		
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

		try {
			response = cluster.invoke(request);
		}finally {
			if(needSwapWhenReturn) {
				//客户端调用产生了future 需要转存进服务端content里;
				if(content.getFuture() != null) {
					RpcInvokeContent back = RpcInvokeContent.getBackupContent();
					back.setFuture(content.getFuture());
				}
				content.clear();
				RpcInvokeContent.swapContent();
			}
		}
		return response;
	}
}

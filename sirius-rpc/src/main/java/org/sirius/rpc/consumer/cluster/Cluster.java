package org.sirius.rpc.consumer.cluster;

import java.util.concurrent.CompletableFuture;

import org.sirius.common.ext.Extensible;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.rpc.consumer.cluster.router.Router;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.load.balance.LoadBalancer;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.api.channel.DirectoryGroupList;
import org.sirius.transport.netty.NettyTcpConnector;

@Extensible
public class Cluster implements Invoker ,ProviderInfoListener {

	private ConsumerConfig consumerConfig;
	private Router router;
	private LoadBalancer loadBalancer;
	private Connector connector;
	private DirectoryGroupList directory;
	private ChannelGroupList channelGroupList;
	private ConsumerProcessor consumerProcessor = new DefaultConsumerProcessor();
	Channel channel;
	
	public Cluster() {
		init();
	}
	private void init() {
		connector = new NettyTcpConnector();
		connector.setConsumerProcessor(consumerProcessor);
		UnresolvedAddress address = new UnresolvedSocketAddress("192.168.1.108",18090);
		channel = connector.connect(address);
	}
	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	@Override
	public Response invoke(Request request) throws Throwable {
		
		channel.send(request);
		CompletableFuture future = new CompletableFuture();
		ResultFutureContent.add(request.invokeId(), future);
		RpcContent.getContent().setFuture(future);
		return new Response(request.invokeId());
	}
	
	@Override
	public void received(ProviderInfo providerInfo) {
		
	}
}

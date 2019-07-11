package org.sirius.rpc.consumer.cluster;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.registry.ProviderInfoListener;

public  abstract class Cluster<T> implements Invoker<T> ,ProviderInfoListener {

	private ConsumerConfig<T> consumerConfig;
	
	public void setConsumerConfig(ConsumerConfig<T> consumerConfig) {
		this.consumerConfig = consumerConfig;
	}
}

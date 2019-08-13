package org.sirius.rpc.consumer.cluster;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.invoker.Invoker;

public  abstract class Cluster<T> implements Invoker<T> {

	protected transient ConsumerConfig<T> consumerConfig;
	
	public Cluster(ConsumerConfig<T> consumerConfig) {
		this.consumerConfig = consumerConfig;
	}
	public void setConsumerConfig(ConsumerConfig<T> consumerConfig) {
		this.consumerConfig = consumerConfig;
	}
}

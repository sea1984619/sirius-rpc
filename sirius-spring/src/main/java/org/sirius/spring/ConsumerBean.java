package org.sirius.spring;

import org.sirius.config.ConsumerConfig;

public class ConsumerBean {

	private ConsumerConfig consumerConfig;

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}
}

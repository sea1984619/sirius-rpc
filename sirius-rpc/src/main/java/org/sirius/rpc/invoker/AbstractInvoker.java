package org.sirius.rpc.invoker;

import org.sirius.rpc.config.AbstractInterfaceConfig;

public abstract class AbstractInvoker<T> implements Invoker<T>{

	protected AbstractInterfaceConfig<T, ?> config;
	public AbstractInvoker(AbstractInterfaceConfig<T, ?> config) {
		this.config = config;
	}
	
	public void setConfig(AbstractInterfaceConfig<T, ?> config) {
		this.config = config;
	}
	public AbstractInterfaceConfig<T, ?> getConfig() {
		return this.config;
	}
}

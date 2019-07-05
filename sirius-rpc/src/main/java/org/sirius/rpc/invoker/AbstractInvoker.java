package org.sirius.rpc.invoker;

import org.sirius.rpc.config.AbstractInterfaceConfig;

public abstract class AbstractInvoker implements Invoker{

	private AbstractInterfaceConfig config;
	public AbstractInvoker(AbstractInterfaceConfig config) {
		this.config = config;
	}
	
	public void setConfig(AbstractInterfaceConfig config) {
		this.config = config;
	}
	public AbstractInterfaceConfig getConfig() {
		return this.config;
	}
}

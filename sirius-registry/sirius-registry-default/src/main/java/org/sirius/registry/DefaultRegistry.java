package org.sirius.registry;

import org.sirius.common.ext.Extension;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.registry.AbstractRegistry;
import org.sirius.rpc.registry.NotifyListener;
import org.sirius.rpc.registry.RegistryService;

@Extension(value = "sirius", singleton = false)
public class DefaultRegistry extends AbstractRegistry{
	
	private RegistryService service;
	
	public RegistryService getService() {
		return service;
	}

	public void setService(RegistryService service) {
		this.service = service;
	}

	public DefaultRegistry(RegistryConfig config) {
		super(config);
	}

	@Override
	protected void init() {
		RegistryConfig registryConfig = getRegistryConfig();
		MethodConfig  methodConfig = new MethodConfig();
		methodConfig.setName("subscribe");
		ArgumentConfig argumentConfig = new ArgumentConfig();
		argumentConfig.setIndex(1)
		              .setCallback(true)
		              .setRetry(true);
		methodConfig.addArgument(argumentConfig);
		ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
		consumerConfig.setDirectUrl(registryConfig.getAddress())
		              .setInterface(RegistryService.class.getName())
		              .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
		              .setTimeout(3000)
		              .addMethod(methodConfig)
		              .initConfigValueCache();
		
		service = consumerConfig.refer();   
	}
	@Override
	protected void doRegister(ProviderConfig<?> config) {
		service.register(config);
		
	}

	@Override
	protected void doUnSubscribe(ConsumerConfig<?> config) {
		service.unSubscribe(config);
	}

	@Override
	protected void doUnregister(ProviderConfig<?> config) {
		service.unRegister(config);
	}

	@Override
	protected void doSubscribe(ConsumerConfig<?> config, NotifyListener listener) {
		service.subscribe(config, listener);
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
	}
}

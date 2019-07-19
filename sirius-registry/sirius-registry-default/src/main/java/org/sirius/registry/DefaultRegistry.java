package org.sirius.registry;

import org.sirius.common.ext.Extension;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.registry.AbstractRegistry;
import org.sirius.rpc.registry.ProviderInfoListener;
import org.sirius.rpc.registry.RegistryFactory;
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
		ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
		consumerConfig.setDirectUrl(registryConfig.getAddress())
		              .setInterface(RegistryService.class.getName())
		              .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
		              .setTimeout(3000);
		service = consumerConfig.refer();   
	}
	@Override
	protected void doRegister(ProviderConfig config) {
		service.register(config);
		
	}

	@Override
	protected void doUnSubscribe(ConsumerConfig config) {
		service.unSubscribe(config);
	}

	@Override
	protected void doUnregister(ProviderConfig config) {
		service.unRegister(config);
	}

	@Override
	protected void doSubscribe(ConsumerConfig config, ProviderInfoListener listener) {
		service.subscribe(config, listener);
	}
	public static void main(String agrs[]) {
		RegistryConfig config = new RegistryConfig();
		config.setAddress("127.0.0.1:5222");
		DefaultRegistry s = (DefaultRegistry)RegistryFactory.getRegistry(config).get(0);
		s.register(null);
		
	}


}

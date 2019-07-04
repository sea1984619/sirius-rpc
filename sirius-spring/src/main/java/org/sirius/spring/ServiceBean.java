package org.sirius.spring;

import org.sirius.rpc.config.ProviderConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServiceBean extends ProviderConfig implements  ApplicationContextAware, InitializingBean, DisposableBean{

	private ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		export();
	}
	
	@Override
	public void destroy() throws Exception {
		
	}
}

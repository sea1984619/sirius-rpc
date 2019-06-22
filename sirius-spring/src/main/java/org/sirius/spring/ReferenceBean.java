package org.sirius.spring;

import org.sirius.config.ConsumerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceBean extends ConsumerConfig implements  FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext context;
	private ConsumerConfig consumerConfig;
	private Class<?> referClass;

	public Class<?> getReferClass() {
		return referClass;
	}

	public void setReferClass(Class<?> referClass) {
		this.referClass = referClass;
	}

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
       this.context = context;		
	}

	@Override
	public Object getObject() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getObjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

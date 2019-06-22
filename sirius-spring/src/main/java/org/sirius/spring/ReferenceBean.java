package org.sirius.spring;

import org.sirius.config.ConsumerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceBean extends ConsumerConfig implements   ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext context;
	private Class<?> referClass;

	public Class<?> getReferClass() {
		return referClass;
	}

	public void setReferClass(Class<?> referClass) {
		this.referClass = referClass;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
       this.context = context;		
	}

//	@Override
//	public Object getObject() throws Exception {
//		return null;
//	}
//
//	@Override
//	public Class getObjectType() {
//		return null;
//	}
//
//	@Override
//	public boolean isSingleton() {
//		return false;
//	}
//	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

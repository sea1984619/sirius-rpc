package org.sirius.spring;

import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.StringUtils;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceBean<T> extends ConsumerConfig<T> implements FactoryBean<T>, ApplicationContextAware, InitializingBean, DisposableBean {
	
	private static final long serialVersionUID = 6747208266832553385L;
	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initRegistry();
	}

	private void initRegistry() {
		String directUrl = getDirectUrl();
		if (directUrl == null) {
			String registry = getRegistry();
			if (registry == null) {
				String[] names = context.getBeanNamesForType(RegistryConfig.class);
				if (names.length == 0) {
					throw new IllegalStateException("<Registry>标签  or directUrl属性,or registry属性全部为空,必须指定一个 ");
				} else {
					for (String name : names) {
						RegistryConfig rc = (RegistryConfig) context.getBean(name);
						registryRef.add(rc);
					}
				}
			} else {
				String[] names = StringUtils.splitWithCommaOrSemicolon(registry);
				for (String name : names) {
					if(context.containsBean(name)) {
						RegistryConfig rc = (RegistryConfig) context.getBean(name);
						registryRef.add(rc);
					}else {
						throw new IllegalStateException("名称为: "+name+" 的registry不存在");
					}
					
				}
			}
		}
	}

	@Override
	public T getObject() throws Exception {
		return  refer();
	}

	@Override
	public Class<?> getObjectType() {
		return proxyClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub

	}

}

package org.sirius.spring;

import java.util.ArrayList;
import java.util.List;

import org.sirius.common.util.ClassUtil;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.RegistryConfig;
import org.sirius.common.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceBean extends ConsumerConfig implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext context;

	private Class<?> referClass;
	private List<RegistryConfig> registryList = new ArrayList<RegistryConfig>();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initReferClass();
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
						registryList.add(rc);
					}
				}
			} else {
				String[] names = StringUtils.splitWithCommaOrSemicolon(registry);
				for (String name : names) {
					if(context.containsBean(name)) {
						RegistryConfig rc = (RegistryConfig) context.getBean(name);
						registryList.add(rc);
					}else {
						throw new IllegalStateException("名称为: "+name+" 的registry不存在");
					}
					
				}
			}
		}
	}

	private void initReferClass() {
		referClass = ClassUtil.forName(getInterface());
	}

	@Override
	public Object getObject() throws Exception {
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return this.referClass;
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

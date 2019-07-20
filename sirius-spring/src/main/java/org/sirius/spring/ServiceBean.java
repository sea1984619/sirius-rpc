package org.sirius.spring;

import org.sirius.common.util.StringUtils;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.ServerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServiceBean extends ProviderConfig implements ApplicationContextAware, InitializingBean, DisposableBean {

	private static final long serialVersionUID = -1012109173695636437L;
	
	private transient ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initRegistry();
		initServer();
		initConfigValueCache();
		export();
	}

	private void initServer() {
		String server = getServer();
		if (server == null) {
			String[] names = context.getBeanNamesForType(ServerConfig.class);
			if (names.length == 0) {
				throw new IllegalStateException("<sirius:srever> element ,or server attribute in <sirius:service> element are both null,must specify one");
			} else {
				for (String name : names) {
					ServerConfig rc = (ServerConfig) context.getBean(name);
					serverRef.add(rc);
				}
			}
		} else {
			String[] names = StringUtils.splitWithCommaOrSemicolon(server);
			for (String name : names) {
				if (context.containsBean(name)) {
					ServerConfig rc = (ServerConfig) context.getBean(name);
					serverRef.add(rc);
				} else {
					throw new IllegalStateException("the sevser bean of " + name + " didn't exist");
				}
			}
		}
		
	}
	private void initRegistry() {
		String registry = getRegistry();
		if (registry == null) {
			String[] names = context.getBeanNamesForType(RegistryConfig.class);
			if (names.length == 0) {
				throw new IllegalStateException("<sirius:Registry> element ,or registry attribute in <sirius:service> element are both null,must specify one");
			} else {
				for (String name : names) {
					RegistryConfig rc = (RegistryConfig) context.getBean(name);
					registryRef.add(rc);
				}
			}
		} else {
			String[] names = StringUtils.splitWithCommaOrSemicolon(registry);
			for (String name : names) {
				if (context.containsBean(name)) {
					RegistryConfig rc = (RegistryConfig) context.getBean(name);
					registryRef.add(rc);
				} else {
					throw new IllegalStateException("the registry bean of " + name + " didn't exist");
				}
			}
		}
	}

	@Override
	public void destroy() throws Exception {

	}
}

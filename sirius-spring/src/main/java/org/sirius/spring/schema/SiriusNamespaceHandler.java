package org.sirius.spring.schema;

import org.sirius.rpc.config.RegistryConfig;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.spring.ReferenceBean;
import org.sirius.spring.ServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SiriusNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("service", new SiriusBeanDefinitionParser(ServiceBean.class));
		registerBeanDefinitionParser("reference", new SiriusBeanDefinitionParser(ReferenceBean.class));
		registerBeanDefinitionParser("registry", new SiriusBeanDefinitionParser(RegistryConfig.class));
		registerBeanDefinitionParser("server", new SiriusBeanDefinitionParser(ServerConfig.class));
	}
}

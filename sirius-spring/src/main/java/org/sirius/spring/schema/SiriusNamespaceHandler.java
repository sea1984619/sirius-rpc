package org.sirius.spring.schema;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.sirius.config.RegistryConfig;
import org.sirius.spring.ReferenceBean;
import org.sirius.spring.ServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SiriusNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("service", new SiriusBeanDefinitionParser(ServiceBean.class));
		registerBeanDefinitionParser("reference", new SiriusBeanDefinitionParser(ReferenceBean.class));
		registerBeanDefinitionParser("registry", new SiriusBeanDefinitionParser(RegistryConfig.class));
		registerBeanDefinitionParser("provider", new SiriusBeanDefinitionParser(ProviderConfig.class));
		registerBeanDefinitionParser("consumer", new SiriusBeanDefinitionParser(ConsumerConfig.class));
	}
}

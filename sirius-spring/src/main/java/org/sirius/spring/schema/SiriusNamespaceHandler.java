package org.sirius.spring.schema;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.ProviderConfig;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SiriusNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("service", new SiriusBeanDefinitionParser(ProviderConfig.class));
		registerBeanDefinitionParser("reference", new SiriusBeanDefinitionParser(ConsumerConfig.class));
	}
}

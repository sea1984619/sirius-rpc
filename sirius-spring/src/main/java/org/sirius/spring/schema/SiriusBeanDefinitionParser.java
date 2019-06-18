package org.sirius.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SiriusBeanDefinitionParser implements BeanDefinitionParser{
	
	private Class<?> clazz;

	public SiriusBeanDefinitionParser(Class<?> clazz) {
		this.clazz = clazz;
	}
	@Override
	public BeanDefinition parse(Element element, ParserContext context) {
		BeanDefinition rb = new RootBeanDefinition();
		String inter = element.getAttribute("interface");
		String className = element.getAttribute("class");
		rb.setBeanClassName(className);
		context.getRegistry().registerBeanDefinition("service",rb);
		return rb;
	}

}

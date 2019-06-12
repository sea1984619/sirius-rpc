package org.sirius.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
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
		// TODO Auto-generated method stub
		return null;
	}

}

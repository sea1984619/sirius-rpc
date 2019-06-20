package org.sirius.spring.schema;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ConsumerBean;

public class SiriusBeanDefinitionParser implements BeanDefinitionParser{
	
	private Class<?> clazz;

	public SiriusBeanDefinitionParser(Class<?> clazz) {
		this.clazz = clazz;
	}
	@Override
	public BeanDefinition parse(Element element, ParserContext context) {
		RootBeanDefinition rb = new RootBeanDefinition();
		rb.setBeanClass(ConsumerBean.class);
		Element el = element;
		
		String id = el.getAttribute("id");
		String version = el.getAttribute("version");
		String group = el.getAttribute("group");
		String _interface = el.getAttribute("interface");
//		rb.getPropertyValues().addPropertyValue("id", id);
//		rb.getPropertyValues().addPropertyValue(version, version);
//		rb.getPropertyValues().addPropertyValue(group, group);
//		rb.getPropertyValues().addPropertyValue("interfaceId", _interface);
		
		
		RootBeanDefinition config = new RootBeanDefinition();
		config.setBeanClass(ConsumerConfig.class);
		context.getRegistry().registerBeanDefinition( "config", config);
		
		BeanReference configR = new  RuntimeBeanReference("config");
		rb.getPropertyValues().addPropertyValue("consumerConfig", configR);
		
		
		if(el.hasChildNodes()) {
			
			
			NodeList nList = el.getChildNodes();
			int length =  nList.getLength();
			ManagedMap<String,RuntimeBeanReference>  methodMap =  new ManagedMap();
			for(int i = 0; i < length ; i++) {
				Node node = nList.item(i);
				if(node.getNodeName().equals("sirius:method") && node instanceof Element) {
					Element melement= (Element) node;
					RootBeanDefinition method = new RootBeanDefinition();
					method.setBeanClass(MethodConfig.class);
					String name = melement.getAttribute("name");
					String timeout = melement.getAttribute("timeout");
					String async = melement.getAttribute("async");
					method.getPropertyValues().add("timeout", timeout);
					method.getPropertyValues().add("name", name);
					method.getPropertyValues().add("async", async);
					context.getRegistry().registerBeanDefinition(_interface +"-"+ name, method);
					methodMap.put(name, new RuntimeBeanReference(_interface +"-"+ name));
				}
			}
			config.getPropertyValues().addPropertyValue("methods",methodMap);
		}
		context.getRegistry().registerBeanDefinition("apple", rb);
		return rb;
	}

}

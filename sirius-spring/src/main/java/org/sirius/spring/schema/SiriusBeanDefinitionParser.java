package org.sirius.spring.schema;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.sirius.common.util.CommonUtils;

public class SiriusBeanDefinitionParser implements BeanDefinitionParser{
	
	private Class<?> clazz;

	public SiriusBeanDefinitionParser(Class<?> clazz) {
		this.clazz = clazz;
	}
	@Override
	public BeanDefinition parse(Element element, ParserContext context) {
		BeanDefinition rb = new RootBeanDefinition();
		Element el = element;
		
		String id = el.getAttribute("id");
		String version = el.getAttribute("version");
		String group = el.getAttribute("group");
		String _interface = el.getAttribute("interface");
		rb.getPropertyValues().addPropertyValue("id", id);
		rb.getPropertyValues().addPropertyValue(version, version);
		rb.getPropertyValues().addPropertyValue(group, group);
		rb.getPropertyValues().addPropertyValue("interfaceId", _interface);
		
		if(el.hasChildNodes()) {
			NodeList nList = el.getChildNodes();
			int length =  nList.getLength();
			for(int i = 0; i < length ; i++) {
				Node node = nList.item(i);
				if(node instanceof Element) {
					Element melement= (Element) node;
					BeanDefinition method = new RootBeanDefinition();
					String name = melement.getAttribute("name");
					String timeout = melement.getAttribute("timeout");
					String async = melement.getAttribute("async");
					method.getPropertyValues().add("timeout", timeout);
					method.getPropertyValues().add("name", name);
					method.getPropertyValues().add("async", async);
				}
			}
		}
		
		return rb;
	}

}

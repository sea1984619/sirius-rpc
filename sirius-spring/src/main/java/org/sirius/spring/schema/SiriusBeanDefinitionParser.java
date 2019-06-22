package org.sirius.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.sirius.common.util.CommonUtils;
import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ReferenceBean;

public class SiriusBeanDefinitionParser implements BeanDefinitionParser {

	private Class<?> beanClass;

	public SiriusBeanDefinitionParser(Class<?> clazz) {
		this.beanClass = clazz;
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");

		if (CommonUtils.isBlank(id)) {
			String generatedBeanName = element.getAttribute("name");
			if (CommonUtils.isBlank(generatedBeanName)) {
				generatedBeanName = element.getAttribute("interface");
			}
			if (CommonUtils.isBlank(generatedBeanName)) {
				generatedBeanName = beanClass.getName();
			}
			id = generatedBeanName;
			int counter = 2;
			while (parserContext.getRegistry().containsBeanDefinition(id)) {
				id = generatedBeanName + (counter++);
			}
		}
		if (id != null && id.length() > 0) {
			if (parserContext.getRegistry().containsBeanDefinition(id)) {
				throw new IllegalStateException("Duplicate spring bean id " + id);
			}
			parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
			System.out.println(id);
			beanDefinition.getPropertyValues().addPropertyValue("id", id);
		}

		parseAttribute(beanDefinition, element, parserContext);
		return beanDefinition;
	}

	private void parseAttribute(RootBeanDefinition beanDefinition, Element element, ParserContext parserContext) {
		NamedNodeMap attrs = element.getAttributes();
		Set<String> attrsNameSet = new HashSet<String>();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node node = attrs.item(i);
			attrsNameSet.add(node.getNodeName());
		}
		Method[] methods = beanClass.getMethods();
		for (Method method : methods) {
			String name = method.getName();
			Class<?>[] paramTypes = method.getParameterTypes();
			if (name.startsWith("set") && name.length() > 3 && paramTypes.length == 1 && Modifier.isPublic(method.getModifiers())) {
				String attrName = name.substring(3, 4).toLowerCase() + name.substring(4);
				if (attrsNameSet.contains(attrName)) {
					String value = element.getAttribute(attrName);
					value = value.trim();
					if (value.length() > 0) {
						Object reference = null;
						if(isPrimitive(paramTypes[0])) {
							reference = value;
						}else {
							if("ref".equals(attrName) && parserContext.getRegistry().containsBeanDefinition(value)) {
								BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(value);
								if (!refBean.isSingleton()) {
                                    throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value + " bean scope to singleton, eg: <bean id=\"" + value + "\" scope=\"singleton\" ...>");
                                }
								reference = new RuntimeBeanReference(value);
							}
						}
						beanDefinition.getPropertyValues().addPropertyValue(attrName, reference);
					}
				}
			}
		}
	}

	private Object parseParameters(NodeList nodeList, RootBeanDefinition beanDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	private ManagedMap parseArguments(String id, NodeList nodeList, RootBeanDefinition beanDefinition,
			ParserContext parserContext) {
		if (nodeList != null && nodeList.getLength() > 0) {
			ManagedMap parameters = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					if ("parameter".equals(node.getNodeName()) || "parameter".equals(node.getLocalName())) {
						if (parameters == null) {
							parameters = new ManagedMap();
						}
						String key = ((Element) node).getAttribute("key");
						String value = ((Element) node).getAttribute("value");
						parameters.put(key, new TypedStringValue(value, String.class));
					}
				}
			}
			return parameters;
		}
		return null;
	}

	private void parseMethods(String id, NodeList nodeList, RootBeanDefinition beanDefinition,
			ParserContext parserContext) {
		// TODO Auto-generated method stub

	}

	private static boolean isPrimitive(Class<?> cls) {
		return cls.isPrimitive() || cls == Boolean.class || cls == Byte.class || cls == Character.class
				|| cls == Short.class || cls == Integer.class || cls == Long.class || cls == Float.class
				|| cls == Double.class || cls == String.class || cls == Date.class || cls == Class.class;
	}

	private static void parseProperties(NodeList nodeList, RootBeanDefinition beanDefinition) {
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					if ("property".equals(node.getNodeName()) || "property".equals(node.getLocalName())) {
						String name = ((Element) node).getAttribute("name");
						if (name != null && name.length() > 0) {
							String value = ((Element) node).getAttribute("value");
							String ref = ((Element) node).getAttribute("ref");
							if (value != null && value.length() > 0) {
								beanDefinition.getPropertyValues().addPropertyValue(name, value);
							} else if (ref != null && ref.length() > 0) {
								beanDefinition.getPropertyValues().addPropertyValue(name,
										new RuntimeBeanReference(ref));
							} else {
								throw new UnsupportedOperationException("Unsupported <property name=\"" + name
										+ "\"> sub tag, Only supported <property name=\"" + name
										+ "\" ref=\"...\" /> or <property name=\"" + name + "\" value=\"...\" />");
							}
						}
					}
				}
			}
		}
	}

}

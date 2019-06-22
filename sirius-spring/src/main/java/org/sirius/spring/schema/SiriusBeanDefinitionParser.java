package org.sirius.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
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

import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.CommonUtils;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ReferenceBean;
import org.sirius.spring.ServiceBean;

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
			beanDefinition.getPropertyValues().addPropertyValue("id", id);
		}

		if (ServiceBean.class.equals(beanClass)) {
            String className = element.getAttribute("class");
            if (className != null && className.length() > 0) {
                RootBeanDefinition classDefinition = new RootBeanDefinition();
                classDefinition.setBeanClass(ClassUtil.forName(className));
                classDefinition.setLazyInit(false);
                parseProperties(element.getChildNodes(), classDefinition);
                beanDefinition.getPropertyValues().addPropertyValue("ref", new BeanDefinitionHolder(classDefinition, id + "Impl"));
            }
        } 
		
		parseAttribute(beanDefinition, element, parserContext,beanClass);
		
		if(element.hasChildNodes()) {
			NodeList childNodes = element.getChildNodes();
			parseChildNodes(id,beanDefinition, childNodes, parserContext);
		}
		
		return beanDefinition;
	}


	private void parseAttribute(RootBeanDefinition beanDefinition, Element element, ParserContext parserContext,Class<?> beanClass) {
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

	private void parseChildNodes(String id, RootBeanDefinition beanDefinition, NodeList childNodes, ParserContext parserContext) {
		for(int i = 0;i < childNodes.getLength();i++) {
			Node node = childNodes.item(i);
			if(node instanceof Element) {
				Element method = (Element) node;
				String nodeName = node.getLocalName();
				if(nodeName.equals("method")) {
					parseMethod(method, beanDefinition, parserContext);
				}
			}
		}
		
	}
	

	@SuppressWarnings("unchecked")
	private void parseMethod(Element element, RootBeanDefinition beanDefinition, ParserContext parserContext) {
		RootBeanDefinition methodDefinition  = new RootBeanDefinition();
		methodDefinition.setBeanClass(MethodConfig.class);
		String innername = "MethodConfig"+ "_" + element.getAttribute("name");
		parserContext.getRegistry().registerBeanDefinition(innername,methodDefinition);
		parseAttribute(methodDefinition, element, parserContext, MethodConfig.class);
		ManagedMap<String, RuntimeBeanReference> methodMap = (ManagedMap<String, RuntimeBeanReference>) beanDefinition.getPropertyValues().get("methods");
		if(methodMap == null) {
			methodMap = new ManagedMap<String, RuntimeBeanReference>();
			beanDefinition.getPropertyValues().add("methods", methodMap);
		}
		methodMap.put(element.getAttribute("name"),new RuntimeBeanReference(innername));
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

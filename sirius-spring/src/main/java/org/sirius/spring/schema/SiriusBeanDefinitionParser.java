package org.sirius.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
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
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.spring.ReferenceBean;
import org.sirius.spring.ServiceBean;

public class SiriusBeanDefinitionParser implements BeanDefinitionParser {

	private Class<?> beanClass;

	public SiriusBeanDefinitionParser(Class<?> clazz) {
		this.beanClass = clazz;
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parse(element,parserContext,beanClass);
	}
	
	public BeanDefinition parse(Element element, ParserContext parserContext,Class<?> beanClass) {
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
			parseChildNodes(beanDefinition, childNodes, parserContext);
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
						if(ClassUtil.isPrimitive(paramTypes[0])) {
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

	private void parseChildNodes(RootBeanDefinition beanDefinition, NodeList childNodes, ParserContext parserContext) {
		for(int i = 0;i < childNodes.getLength();i++) {
			Node node = childNodes.item(i);
			if(node instanceof Element) {
				Element element = (Element) node;
				String nodeName = node.getLocalName();
				if(nodeName.equals("method")) {
					parseMethod(element, beanDefinition, parserContext);
				}else if(nodeName.equals("argument")) {
					parseArgument(element, beanDefinition, parserContext);
				}else if(nodeName.equals("parameter")) {
					parseParameter(element, beanDefinition, parserContext);
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void parseParameter(Element element, RootBeanDefinition beanDefinition, ParserContext parserContext) {
		 ManagedMap<String, TypedStringValue> parameters = (ManagedMap<String, TypedStringValue>) beanDefinition.getPropertyValues().get("parameters");
		 if(parameters == null) {
			    parameters = new ManagedMap<String, TypedStringValue>();
				beanDefinition.getPropertyValues().add("parameters", parameters);
			}
		 String key = element.getAttribute("key");
         String value = element .getAttribute("value");
         parameters.put(key, new TypedStringValue(value, String.class));
	}

	@SuppressWarnings("unchecked")
	private void parseArgument(Element element, RootBeanDefinition beanDefinition, ParserContext parserContext) {
		RootBeanDefinition methodDefinition = (RootBeanDefinition) parse(element, parserContext, ArgumentConfig.class);
		ManagedList<BeanDefinitionHolder> arguments = (ManagedList<BeanDefinitionHolder>) beanDefinition.getPropertyValues().get("arguments");
		if(arguments == null) {
			arguments = new ManagedList<BeanDefinitionHolder>();
			beanDefinition.getPropertyValues().add("arguments", arguments);
		}
		String index = element.getAttribute("index");
		arguments.add(new BeanDefinitionHolder(methodDefinition, ArgumentConfig.class.getName()+ "_" + index));
	}

	@SuppressWarnings("unchecked")
	private void parseMethod(Element element, RootBeanDefinition beanDefinition, ParserContext parserContext) {
		
		RootBeanDefinition methodDefinition = (RootBeanDefinition) parse(element, parserContext, MethodConfig.class);
		ManagedMap<String, BeanDefinitionHolder> methodMap = (ManagedMap<String, BeanDefinitionHolder>) beanDefinition.getPropertyValues().get("methods");
		if(methodMap == null) {
			methodMap = new ManagedMap<String, BeanDefinitionHolder>();
			beanDefinition.getPropertyValues().add("methods", methodMap);
		}
		String name = element.getAttribute("name");
		methodMap.put(name,new BeanDefinitionHolder(methodDefinition, MethodConfig.class.getName()+ "_" + name));
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

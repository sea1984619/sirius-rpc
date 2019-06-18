package org.sirius.spring.schema;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext(
	                "spring.xml");
//		 
//		 System.out.println(ctx.containsBeanDefinition("service"));
//		 System.out.println(ctx.getBeanDefinitionNames());
	}
}

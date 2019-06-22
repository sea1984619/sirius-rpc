package org.sirius.spring.schema;

import java.util.Map;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ReferenceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

		 ReferenceBean apple = (ReferenceBean) ctx.getBean("apple");
		 Map<String ,MethodConfig> map = apple.getMethods();
		 for(MethodConfig mc : map.values()) {
			System.out.println(mc.getTimeout()); 
		 }
		 
	}
}

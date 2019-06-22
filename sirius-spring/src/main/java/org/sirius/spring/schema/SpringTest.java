package org.sirius.spring.schema;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ReferenceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

		 ReferenceBean bean = (ReferenceBean) ctx.getBean("apple");
		
		 System.out.println(bean.getId());
		 System.out.println(bean.getInterface());
		 
		 
	}
}

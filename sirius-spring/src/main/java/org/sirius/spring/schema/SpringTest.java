package org.sirius.spring.schema;

import org.sirius.config.ConsumerConfig;
import org.sirius.config.MethodConfig;
import org.sirius.spring.ConsumerBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext(
	                "spring.xml");

		 ConsumerBean bean = (ConsumerBean) ctx.getBean("apple");
		 ConsumerConfig con = bean.getConsumerConfig();
		 MethodConfig m = (MethodConfig) con.getMethods().get("get");
		 System.out.println(m.getTimeout());
		 
		 
	}
}

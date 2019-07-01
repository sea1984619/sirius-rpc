package org.sirius.spring.schema;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.sirius.spring.ReferenceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

		 ReferenceBean apple = (ReferenceBean) ctx.getBean("&apple");
		 System.out.println(apple); 
	}
}

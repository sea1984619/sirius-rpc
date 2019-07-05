package org.sirius.spring.schema;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sirius.rpc.config.ServerConfig;
import org.sirius.spring.Apple;
import org.sirius.spring.AppleImpl;
import org.sirius.spring.ReferenceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

//		 Apple apple = (Apple) ctx.getBean("apple");
//		 AppleImpl d = new AppleImpl();
//		 apple.eat(d);
		 ServerConfig sc = ctx.getBean(ServerConfig.class);
		 System.out.println(sc.getPort());
	}
}

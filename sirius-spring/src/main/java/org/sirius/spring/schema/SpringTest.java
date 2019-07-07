package org.sirius.spring.schema;


import org.sirius.rpc.config.ServerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {

	public static void main(String[] args) {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

//		 Apple apple = (Apple) ctx.getBean("apple");
//		 AppleImpl d = new AppleImpl();
//		 apple.eat(d);
		 ServerConfig sc = ctx.getBean(ServerConfig.class);
	}
}

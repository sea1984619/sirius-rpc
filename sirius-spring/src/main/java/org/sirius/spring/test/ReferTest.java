package org.sirius.spring.test;

import java.util.concurrent.ExecutionException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ReferTest {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring2.xml");
		 Shop shop = (Shop) ctx.getBean("shop");
         Apple apple = shop.buyApple("黑色");		
         System.out.println(apple.color);
//	
	}
}

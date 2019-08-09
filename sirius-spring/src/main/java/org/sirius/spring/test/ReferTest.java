package org.sirius.spring.test;


import org.sirius.rpc.RpcInvokeContent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ReferTest {

	public static void main(String args[]) throws Throwable {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring2.xml");
		 Shop shop = (Shop) ctx.getBean("shop");
         shop.buyApple("黑色");	
         Apple apple =  (Apple) RpcInvokeContent.getContent().getFuture().getResult();
         System.out.println(apple.color);
         Thread.sleep(500000);
//	
	}
}

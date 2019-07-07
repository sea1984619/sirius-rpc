package org.sirius.spring.schema;

import java.util.concurrent.ExecutionException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ReferTest {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		 ApplicationContext ctx = new ClassPathXmlApplicationContext("spring2.xml");
		 Person person = (Person) ctx.getBean("person");
		 ListenerImpl listener = new  ListenerImpl();
		 person.eat(listener);
		 Thread.sleep(2000000);
	}
}

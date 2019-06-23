package org.sirius.spring.schema;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sirius.config.ArgumentConfig;
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
			List<ArgumentConfig> acs =  (List) mc.getArguments();
			for(ArgumentConfig ac:acs ) {
				System.out.println(ac.getIndex()); 
			}
			Map<String,String> p = mc.getParameters();

			for(Entry<String, String> s:p.entrySet()) {
				System.out.println(s.getKey());
				System.out.println(s.getValue());
			}
			System.out.println("第三方"+p.size());
		 }
		 
	}
}

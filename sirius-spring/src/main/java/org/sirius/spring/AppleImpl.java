package org.sirius.spring;

import java.lang.reflect.Method;

public class AppleImpl implements Apple {


	@Override
	public void get(String d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eat(String d) {
		// TODO Auto-generated method stub
		
	}
 public static void main(String arg[]) {
	 Class clazz = AppleImpl.class;
	 Method[] methods = clazz.getMethods();
	 for(Method m : methods) {
		 System.out.println(m.getName());
	 }
 }
}

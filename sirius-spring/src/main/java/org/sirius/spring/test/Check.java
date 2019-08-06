package org.sirius.spring.test;

public class Check {

	public void check(String color) {
		System.out.println("要买的颜色为" + color);
	}
	public void checkreturn(Apple a) {
		System.out.println("检查买回来的水果" + a.color);
	}
}

package org.sirius.spring.test;

public class Check {

	public void check(String color) {
		System.out.println("要买的颜色为" + color);
	}
	public void checkreturn(Apple a,String c) {
		System.out.println("检查买回来的水果,要买的颜色为" +c);
		System.out.println("买回来的水果颜色" + a.color);
		a.color = "蓝色";
	}
}

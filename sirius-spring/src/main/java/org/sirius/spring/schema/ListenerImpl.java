package org.sirius.spring.schema;

public class ListenerImpl implements EatListener ,java.io.Serializable{

	private static final long serialVersionUID = 5604213141012777712L;

	@Override
	public void onEat(Apple apple) {
		System.out.println("苹果的颜色:" + apple.color);
	}
}

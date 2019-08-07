package org.sirius.spring.test;

public class ShopImpl implements Shop{

	@Override
	public Apple buyApple(String color) {
		return new Apple(color);
	}
	
}

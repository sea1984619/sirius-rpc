package org.sirius.spring.test;

public class ShopImpl implements Shop{

	@Override
	public Apple buyApple() {
		return new Apple("红色");
	}
}

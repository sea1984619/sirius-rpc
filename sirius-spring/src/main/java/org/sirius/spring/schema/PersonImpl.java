package org.sirius.spring.schema;

public class PersonImpl implements Person{


	@Override
	public void eat(EatListener listener) {
		Apple apple = new Apple("红色");
		for(int i = 0; i< 100 ;i++) {
			listener.onEat(apple);
			
		}
	}
}

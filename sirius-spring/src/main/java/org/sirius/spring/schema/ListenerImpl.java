package org.sirius.spring.schema;

public class ListenerImpl implements EatListener ,java.io.Serializable{

	private static final long serialVersionUID = 5604213141012777712L;

	@Override
	public void onEat(String s) {
		System.out.println(s);
	}
}

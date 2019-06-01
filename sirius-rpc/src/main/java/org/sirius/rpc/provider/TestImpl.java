package org.sirius.rpc.provider;

public class TestImpl implements Test {

	public TestImpl() {
		
	}
	@Override
	public Apple getApple() {
		return new Apple();
	}

}

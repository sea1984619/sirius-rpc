package org.sirius.rpc.provider;

public class TestImpl implements Test {

	@Override
	public Apple getApple() {
		return new Apple();
	}

}

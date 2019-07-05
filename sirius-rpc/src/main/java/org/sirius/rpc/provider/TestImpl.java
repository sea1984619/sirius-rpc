package org.sirius.rpc.provider;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public class TestImpl implements Test {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(TestImpl.class);
	public TestImpl() {
		
	}
	@Override
	public Apple getApple() {
		return new Apple();
	}

	public static void main (String args[]) {
		TestImpl t = new TestImpl();
//        logger.info("Method:Hello");
		
		t.getApple();
		System.out.println("dfds");
	}
}

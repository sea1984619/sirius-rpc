package org.sirius.transport.netty;

import io.netty.util.internal.PlatformDependent;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		System.out.println(PlatformDependent.hasDirectBufferNoCleanerConstructor());
//		NettyTcpAcceptor server = new NettyTcpAcceptor();
//		server.start();
	}

}

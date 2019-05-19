package org.sirius.transport.netty;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		NettyTcpAcceptor server = new NettyTcpAcceptor();
		server.start();
	}

}

package org.sirius.transport.netty;

import java.util.concurrent.ThreadFactory;

import org.sirius.transport.api.Connection;
import org.sirius.transport.api.UnresolvedAddress;

import io.netty.channel.EventLoopGroup;

public class NettyTcpConnector extends NettyConnecter {

	public NettyTcpConnector(Protocol protocol) {
		super(protocol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Connection connect(UnresolvedAddress address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection connect(UnresolvedAddress address, boolean async) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIoRatio(int workerIoRatio) {
		// TODO Auto-generated method stub

	}

	@Override
	protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {
		// TODO Auto-generated method stub
		return null;
	}

}

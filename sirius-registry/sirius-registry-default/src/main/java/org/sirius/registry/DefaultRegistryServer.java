package org.sirius.registry;


import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.registry.api.RegistryService;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.ProviderProxyUtil;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.netty.NettyTcpAcceptor;


public class DefaultRegistryServer extends NettyTcpAcceptor {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryServer.class);
	
	private static int default_port = 20080;
    private ProviderProcessor processor = new RegistryProcessor();
    private ProviderProxyInvoker<?> registryInvoker; 
    private RegistryService service = new DefaultRegistryService();
	
	DefaultRegistryServer(){
		this(default_port);
	}

	DefaultRegistryServer(int port) {
		super(port);
		this.setProcessor(processor);
		registryInvoker = (ProviderProxyInvoker<?>) ProviderProxyUtil.getInvoker(service, RegistryService.class);
	}

   class RegistryProcessor implements ProviderProcessor{

	@Override
	public void handlerRequest(Channel channel, Request request)  {
		Response res = null;
		try {
			res = registryInvoker.invoke(request);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			channel.send(res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handlerException(Channel channel, Throwable e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	   
   }
}

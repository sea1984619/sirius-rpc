package org.sirius.rpc.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public class ServerFactory {

    private final static InternalLogger   LOGGER     = InternalLoggerFactory.getInstance(ServerFactory.class);
    private final static ConcurrentMap<String, RpcServer> SERVER_MAP = new ConcurrentHashMap<String, RpcServer>(); 
    
    
}

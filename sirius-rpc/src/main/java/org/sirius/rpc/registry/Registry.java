package org.sirius.rpc.registry;

import org.sirius.common.ext.Extensible;

@Extensible
public interface Registry  extends RegistryService{

	 boolean start();
	 
	 void shutdown();
}

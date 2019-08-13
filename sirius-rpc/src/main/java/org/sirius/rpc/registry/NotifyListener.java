package org.sirius.rpc.registry;

public interface NotifyListener {

    void providerOnLine(ProviderInfo providerInfo);
	
	void providerOffLine(ProviderInfo providerInfo);
	
	void routerAdd(String router);
	
	void routerDelete(String router);
	
	void configAdd(String config);
	
	void ConfigDelete(String config);
	
	
}

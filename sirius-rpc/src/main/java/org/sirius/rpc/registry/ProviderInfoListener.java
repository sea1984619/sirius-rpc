package org.sirius.rpc.registry;

public interface ProviderInfoListener {

	void notifyOnLine(ProviderInfoGroup providerInfoGroup);
	
	void notifyOffLine(ProviderInfoGroup providerInfoGroup);
	
	void notifyConfiguration(ProviderInfoGroup providerInfoGroup);
	
	void notifyRouter(ProviderInfoGroup providerInfoGroup);
	
	void notifyUpdate(ProviderInfoGroup providerInfoGroup);
}

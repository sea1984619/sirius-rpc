package org.sirius.rpc.registry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.Maps;
import org.sirius.common.util.NetUtils;
import org.sirius.rpc.config.RegistryConfig;

import io.netty.util.internal.StringUtil;

public class RegistryFactory {

	private static Map<String, Registry> registrys = Maps.newConcurrentMap();
	
	private static String DEFAULT_KEY ="sirius"; 
	
	public static List<Registry> getRegistry(RegistryConfig config) {
		String protocol = config.getProtocol();
		if(StringUtil.isNullOrEmpty(protocol)) {
			protocol = DEFAULT_KEY;
		}
		String originalAddress = config.getAddress();
		List<InetSocketAddress>  addressList = NetUtils.getIpListByRegistry(originalAddress);
		List<Registry> registryList = new ArrayList<Registry>();
		for(InetSocketAddress address : addressList) {
			String key = protocol + NetUtils.toAddressString(address);
			Registry registry = registrys.get(key);
			if(registry == null) {
				synchronized(registrys) {
					if(registry == null) {
						ExtensionLoader<Registry> loader = ExtensionLoaderFactory.getExtensionLoader(Registry.class);
						registry = loader.getExtension(protocol,new Class[]{RegistryConfig.class},new Object[]{config});
						registrys.putIfAbsent(key, registry);
					}
				}
			}
			registryList.add(registry);
		}
		return registryList;
	}
}

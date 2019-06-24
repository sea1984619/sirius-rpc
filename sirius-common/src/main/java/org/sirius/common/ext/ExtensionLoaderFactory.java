package org.sirius.common.ext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtensionLoaderFactory {

	  private ExtensionLoaderFactory() {
		  
	    }
	  
	  private static final ConcurrentMap<Class, ExtensionLoader> LOADER_MAP = new ConcurrentHashMap<Class, ExtensionLoader>();
	  
	  public static <T> ExtensionLoader getExtensionLoader(Class<T> clazz) {
	        ExtensionLoader loader = LOADER_MAP.get(clazz);
	        if (loader == null) {
	            synchronized (ExtensionLoaderFactory.class) {
	                loader = LOADER_MAP.get(clazz);
	                if (loader == null) {
	                    loader = new ExtensionLoader(clazz);
	                    LOADER_MAP.put(clazz, loader);
	                }
	            }
	        }
	        return loader;
	    }
}

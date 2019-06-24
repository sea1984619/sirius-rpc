package org.sirius.common.ext;

import java.lang.reflect.Modifier;
import java.util.List;
import java.lang.RuntimeException;

public class ExtensionLoader {

	private Class<?> clazz;
	
	private List<?> extensions;
	
	public ExtensionLoader(Class<?> clazz) {
		if( !(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))) {
			throw new RuntimeException(clazz +" must be a interface or a abstract class");
		}
		if(clazz.getAnnotation(Extensible.class) == null) {
			throw new RuntimeException(clazz +" must has @Extensible annotation ");
		}
		this.clazz = clazz;
		load();
	}

	private void load() {
		
		
	}
	
}
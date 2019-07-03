package org.sirius.common.ext;

import java.util.Comparator;

public class ExtensionComparetor<T> implements Comparator<T>{

	@Override
	public int compare(T o1, T o2) {
		Extension ext1 = o1.getClass().getAnnotation(Extension.class);
		Extension ext2 = o2.getClass().getAnnotation(Extension.class);
		if(ext1.order() > ext2.order()) {
			return 1;
		}else if(ext1.order() == ext2.order()) {
			return 0;
		}else
		    return -1;
	}

}

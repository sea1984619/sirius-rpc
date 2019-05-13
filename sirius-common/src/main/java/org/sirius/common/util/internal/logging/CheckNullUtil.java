package org.sirius.common.util.internal.logging;

public final class CheckNullUtil {

	public static <T> T check(T t) {
		if(t==null) {
			 throw new NullPointerException();
		}
		return t;
	}
	
	public static <T> T check(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }
	
	 private CheckNullUtil() {}
}

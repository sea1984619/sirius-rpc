package org.sirius.common.util;

import java.util.ArrayList;
/*
 * Lists工具类, 仅仅是对guava工具类的包装
 */
public class Lists {

	public static <E> ArrayList<E> newArrayList() {
	    return new ArrayList<>();
	}
	
	public static <E> ArrayList<E> newArrayList(E... elements) {
	   
	    return com.google.common.collect.Lists.newArrayList(elements);
	}
	
	 public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
		   
		return com.google.common.collect.Lists.newArrayList(elements);
	 }
}

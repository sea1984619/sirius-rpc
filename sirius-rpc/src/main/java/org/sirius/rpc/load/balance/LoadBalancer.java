package org.sirius.rpc.load.balance;

import java.util.List;

public interface LoadBalancer<T>{

	T select(List<T> list);
	
	/*
	 * 带权重的负载均衡
	 * @param weightArray  权重数组
	 */
	T select(List<T> list,int[] weightArray);
}

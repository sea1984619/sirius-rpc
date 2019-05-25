package org.sirius.rpc.consumer.invoker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.sirius.rpc.JavassistProxyFactory;
import org.sirius.rpc.RpcContent;

public class Test4 {

	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		JavassistProxyFactory f = new JavassistProxyFactory();
		AbstractInvoker  invoker = new AbstractInvoker();
		Shop shop = null;
		try {
			shop = (Shop) f.getProxy(invoker, Shop.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shop.buyBook();
		CompletableFuture<Object> result = RpcContent.get();
		System.out.println("result1:"+result);
		//System.out.println("结果1:"+result.get());
		
		shop.buyPig();
		CompletableFuture<Object> result2 = RpcContent.get();
		System.out.println("result2:"+result2);
//		System.out.println("结果2:"+result2.get());

	}

}

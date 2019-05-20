package org.sirius.common.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.sirius.common.util.internal.InternalThreadLocal;


/*
 * 适用于多线程环境下的整数序号生成器 ,
 * 每个线程有自己的序号区间, 按照固定步长
 */
public class IntegerSequencer {
   
	private static final int defalut_step = 64 ;
	private int step;
	AtomicInteger rootCounter ;
	InternalThreadLocal<InnerCounter> inner = new InternalThreadLocal<InnerCounter>() {
		 @Override
	        protected InnerCounter initialValue() {
	            return new InnerCounter();
	        }
	};
	
	public IntegerSequencer() {
		this(defalut_step,0);
	}
	
	public IntegerSequencer(int initialValue) {
		this(defalut_step ,initialValue);
	}
	
	public IntegerSequencer(int step ,int initialValue) {
		this.step = step;
		this.rootCounter = new AtomicInteger(initialValue);
		
	}
	//允许溢出
	public int next() {
		return inner.get().next();
	}
	//不允许溢出
	public int nextAndNonNegative() {
		return inner.get().nextAndNonNegative();
	}
	
	private final class InnerCounter {
		private int base;
		private int innerValue;
		
		public InnerCounter () {
			base = rootCounter.getAndAdd(step);
		}
		public int next() {
			int next =  ++innerValue + base;
			if(innerValue == step) {
				base = rootCounter.getAndAdd(step);
				innerValue = 0;
			}
			return next;
		}
		public int nextAndNonNegative() {
			int next =  ++innerValue + base;
			if(innerValue == step) {
				base = rootCounter.getAndAdd(step);
				innerValue = 0;
				
				int max = base + step;
					if (base < 0 || max < 0) {
						rootCounter.compareAndSet(max, 0);
						base = rootCounter.getAndAdd(step);
						innerValue = 0;
					}
				
			}
			return next;
		}
	}
	
	 
    public static void main(String arg[]) {
    	IntegerSequencer s = new IntegerSequencer();
    	new Thread(()->{for(int i=0 ;i<100; i++ )
    	{
    		System.out.println("a"+s.next());
    	}}).start();;
    	new Thread(()->{for(int i=0 ;i<100; i++ )
    	{
    		System.out.println(s.next());
    	}}).start();;
    }
}

package org.sirius.common.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.sirius.common.util.internal.InternalThreadLocal;

public class LongSequencer {
	 
		private static final int defalut_step = 64 ;
		private int step;
		AtomicLong rootCounter ;
		InternalThreadLocal<InnerCounter> inner = new InternalThreadLocal<InnerCounter>() {
			 @Override
		        protected InnerCounter initialValue() {
		            return new InnerCounter();
		        }
		};
		
		public LongSequencer() {
			this(defalut_step,0);
		}
		
		public LongSequencer(long initialValue) {
			this(defalut_step ,initialValue);
		}
		
		public LongSequencer(int step ,long initialValue) {
			this.step = step;
			this.rootCounter = new AtomicLong(initialValue);
			
		}
		//允许溢出
		public long next() {
			return inner.get().next();
		}
		//不允许溢出
		public long nextAndNonNegative() {
			return inner.get().nextAndNonNegative();
		}
		
		private final class InnerCounter {
			private long base;
			private long innerValue;
			
			public InnerCounter () {
				base = rootCounter.getAndAdd(step);
			}
			public long next() {
				long next =  ++innerValue + base;
				if(innerValue == step) {
					base = rootCounter.getAndAdd(step);
					innerValue = 0;
				}
				return next;
			}
			public long nextAndNonNegative() {
				long next =  ++innerValue + base;
				if(innerValue == step) {
					base = rootCounter.getAndAdd(step);
					innerValue = 0;
					
					long max = base + step;
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
	    	LongSequencer s = new LongSequencer();
	    	new Thread(()->{for(long i=0 ;i<100; i++ )
	    	{
	    		System.out.println("a"+s.next());
	    	}}).start();;
	    	new Thread(()->{for(int i=0 ;i<100; i++ )
	    	{
	    		System.out.println(s.next());
	    	}}).start();;
	    }
	}

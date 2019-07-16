package org.sirius.spring.schema;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersonImpl implements Person{

	private final Map<String, EatListener> listeners = new ConcurrentHashMap<String, EatListener>();  
	public PersonImpl() {
		Thread t = new Thread(new Runnable() {  
            public void run() {  
                while(true) {  
                    try {  
                        for(Map.Entry<String, EatListener> entry : listeners.entrySet()){  
                           try {  
                               entry.getValue().onEat(getChanged(entry.getKey()));  
                           } catch (Throwable t) {  
                               System.out.println("发生错误");
                           }  
                        }  
                        Thread.sleep(15000); // 定时触发变更通知  
                    } catch (Throwable t) { // 防御容错  
                        t.printStackTrace();  
                    }  
                }  
            }

			
        });  
        t.setDaemon(true);  
        t.start();  
	}

	
	public void addListener(String key, EatListener listener) {  
		System.out.println(listener.hashCode());
        listeners.put(String.valueOf(listener.hashCode()), listener);  
        System.out.println("当前数量为:"+listeners.size());
    }  
       
    private String getChanged(String key) {  
        return "Changed: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());  
    }


	@Override
	public void eat(EatListener listener) {
		addListener("one",listener);
	}   
}

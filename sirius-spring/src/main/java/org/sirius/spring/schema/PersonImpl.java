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
                        	   String s = getChanged(entry.getKey());
                               entry.getValue().onEat(s); 
                           } catch (Throwable t) {  
                               t.printStackTrace();
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
        listeners.put(String.valueOf(listener.hashCode()), listener);
    }  
       
    private String getChanged(String key) {  
        return "Changed: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());  
    }


	@Override
	public void eat(EatListener listener) {
		addListener("one",listener);
	}   
}

package org.sirius.rpc.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodConfig extends AbstractIdConfig implements Serializable {

    private static final long      serialVersionUID = -8594337650648536897L;

    /*-------------配置项开始----------------*/
    /**
     * 方法名称，无法做到重载方法的配置
     */
    private String                 name;

    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String>  parameters;

    /**
     * The Timeout. 远程调用超时时间(毫秒)
     */
    protected Integer              timeout;

    /**
     * The Retries. 失败后重试次数
     */
    protected Integer              retries;

    /**
     * 调用方式
     */
    protected String               invokeType;

    /**
     * The Validation. 是否jsr303验证
     */
    protected Boolean              validation;

    /**
     * 是否异步
     */
    protected Boolean              async;


	/**
     * 最大并发执行（不管服务端还是客户端）
     */
    protected Integer              concurrents;

    /**
     * 是否启用客户端缓存
     */
    protected Boolean              cache;

    /**
     * 是否启动压缩
     */
    protected String               compress;

 // whether need to return
    private Boolean isReturn;

    // callback instance when async-call is invoked
    private Object oninvoke;

    // callback method when async-call is invoked
    private String oninvokeMethod;

    // callback instance when async-call is returned
    private Object onreturn;

    // callback method when async-call is returned
    private String onreturnMethod;

    // callback instance when async-call has exception thrown
    private Object onthrow;

    // callback method when async-call has exception thrown
    private String onthrowMethod;
    
	private volatile List<ArgumentConfig> arguments;

    /*-------------配置项结束----------------*/
    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public MethodConfig setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public MethodConfig setParameters(Map<String, String> parameters) {
        if (this.parameters == null) {
            this.parameters = new ConcurrentHashMap<String, String>();
            this.parameters.putAll(parameters);
        }
        return this;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public MethodConfig setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Gets retries.
     *
     * @return the retries
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     */
    public MethodConfig setRetries(Integer retries) {
        this.retries = retries;
        return this;
    }

    /**
     * Gets invoke type.
     *
     * @return the invoke type
     */
    public String getInvokeType() {
        return invokeType;
    }

    /**
     * Sets invoke type.
     *
     * @param invokeType the invoke type
     * @return the invoke type
     */
    public MethodConfig setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public Integer getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     */
    public MethodConfig setConcurrents(Integer concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets cache.
     *
     * @return the cache
     */
    public Boolean getCache() {
        return cache;
    }

    /**
     * Sets cache.
     *
     * @param cache the cache
     */
    public MethodConfig setCache(Boolean cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Sets validation.
     *
     * @param validation the validation
     */
    public MethodConfig setValidation(Boolean validation) {
        this.validation = validation;
        return this;
    }

    /**
     * Gets validation.
     *
     * @return the validation
     */
    public Boolean getValidation() {
        return validation;
    }


    public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}
	
    /**
     * Gets compress.
     *
     * @return the compress
     */
    public String getCompress() {
        return compress;
    }

    /**
     * Sets compress.
     *
     * @param compress the compress
     */
    public MethodConfig setCompress(String compress) {
        this.compress = compress;
        return this;
    }

    public Object getOnreturn() {
        return onreturn;
    }

    public void setOnreturn(Object onreturn) {
        this.onreturn = onreturn;
    }

    public String getOnreturnMethod() {
        return onreturnMethod;
    }

    public void setOnreturnMethod(String onreturnMethod) {
        this.onreturnMethod = onreturnMethod;
    }

    public Object getOnthrow() {
        return onthrow;
    }

    public void setOnthrow(Object onthrow) {
        this.onthrow = onthrow;
    }

    public String getOnthrowMethod() {
        return onthrowMethod;
    }

    public void setOnthrowMethod(String onthrowMethod) {
        this.onthrowMethod = onthrowMethod;
    }

    public Object getOninvoke() {
        return oninvoke;
    }

    public void setOninvoke(Object oninvoke) {
        this.oninvoke = oninvoke;
    }

    public String getOninvokeMethod() {
        return oninvokeMethod;
    }

    public void setOninvokeMethod(String oninvokeMethod) {
        this.oninvokeMethod = oninvokeMethod;
    }

    public Boolean isReturn() {
        return isReturn;
    }

    public void setReturn(Boolean isReturn) {
        this.isReturn = isReturn;
    }

    /**
     * Sets parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public MethodConfig setParameter(String key, String value) {
        if (parameters == null) {
            parameters = new ConcurrentHashMap<String, String>();
        }
        parameters.put(key, value);
        return this;
    }

    /**
     * Gets parameter.
     *
     * @param key the key
     * @return the value
     */
    public String getParameter(String key) {
        return parameters == null ? null : parameters.get(key);
    }
    
    public List<ArgumentConfig> getArguments() {
  		return arguments;
  	}

  	public MethodConfig setArguments(List<ArgumentConfig> arguments) {
  		this.arguments = arguments;
  		return this;
  	}
  	public MethodConfig addArgument(ArgumentConfig argument) {
  		if(arguments == null) {
  			synchronized(this) {
  				if(arguments == null) {
  					arguments = new ArrayList<>();
  				}
  			}
  		}
  		arguments.add(argument);
  		return this;
  	}
}

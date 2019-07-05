package org.sirius.rpc.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.sirius.common.util.ClassUtil;
import org.sirius.rpc.Filter;
import org.sirius.rpc.FilterChain;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;

/**
 * 服务提供者配置
 */
public class ProviderConfig<T> extends AbstractInterfaceConfig<T, ProviderConfig<T>> implements Serializable {

	/**
	 * The constant serialVersionUID.
	 */
	private static final long serialVersionUID = -3058073881775315962L;

	/*---------- 参数配置项开始 ------------*/

	/**
	 * 接口实现类引用
	 */
	protected transient T ref;

	protected String server;
	
	protected List<ServerConfig> serverRef = new ArrayList<ServerConfig>();
	
	/**
	 * 服务发布延迟,单位毫秒，默认0，配置为-1代表spring加载完毕（通过spring才生效）
	 */
	protected int delay;
	/**
	 * 权重
	 */
	protected int weight;

	/**
	 * 包含的方法
	 */
	protected String include;

	/**
	 * 不发布的方法列表，逗号分隔
	 */
	protected String exclude;

	/**
	 * 是否动态注册，默认为true，配置为false代表不主动发布，需要到管理端进行上线操作
	 */
	protected boolean dynamic;

	/**
	 * 服务优先级，越大越高
	 */
	protected int priority;

	/**
	 * 启动器
	 */
	protected String bootstrap;

	/**
	 * 自定义线程池
	 */
	protected transient ThreadPoolExecutor executor;

	/**
	 * whitelist blacklist
	 */

	/*-------- 下面是方法级可覆盖配置 --------*/

	/**
	 * 服务端执行超时时间(毫秒)，不会打断执行线程，只是打印警告
	 */
	protected int timeout;

	/**
	 * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
	 */
	protected int concurrents;
	

	/*---------- 参数配置项结束 ------------*/

	/**
	 * 方法名称：是否可调用
	 */
	protected transient volatile ConcurrentMap<String, Boolean> methodsLimit;

	/**
	 * Gets ref.
	 *
	 * @return the ref
	 */
	public T getRef() {
		return ref;
	}

	/**
	 * Sets ref.
	 *
	 * @param ref
	 *            the ref
	 * @return the ref
	 */
	public ProviderConfig<T> setRef(T ref) {
		this.ref = ref;
		return this;
	}

	public List<ServerConfig> getServerRef() {
		return serverRef;
	}

	public void setServerRef(List<ServerConfig> serverRef) {
		this.serverRef = serverRef;
	}
	
	/**
	 * Gets delay.
	 *
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Sets delay.
	 *
	 * @param delay
	 *            the delay
	 * @return the delay
	 */
	public ProviderConfig<T> setDelay(int delay) {
		this.delay = delay;
		return this;
	}

	/**
	 * Gets weight.
	 *
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Sets weight.
	 *
	 * @param weight
	 *            the weight
	 * @return the weight
	 */
	public ProviderConfig<T> setWeight(int weight) {
		this.weight = weight;
		return this;
	}

	/**
	 * Gets include.
	 *
	 * @return the include
	 */
	public String getInclude() {
		return include;
	}

	/**
	 * Sets include.
	 *
	 * @param include
	 *            the include
	 * @return the include
	 */
	public ProviderConfig<T> setInclude(String include) {
		this.include = include;
		return this;
	}

	/**
	 * Gets exclude.
	 *
	 * @return the exclude
	 */
	public String getExclude() {
		return exclude;
	}

	/**
	 * Sets exclude.
	 *
	 * @param exclude
	 *            the exclude
	 * @return the exclude
	 */
	public ProviderConfig<T> setExclude(String exclude) {
		this.exclude = exclude;
		return this;
	}

	/**
	 * Is dynamic boolean.
	 *
	 * @return the boolean
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * Sets dynamic.
	 *
	 * @param dynamic
	 *            the dynamic
	 * @return the dynamic
	 */
	public ProviderConfig<T> setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
		return this;
	}

	/**
	 * Gets priority.
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets priority.
	 *
	 * @param priority
	 *            the priority
	 * @return the priority
	 */
	public ProviderConfig<T> setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Gets bootstrap.
	 *
	 * @return the bootstrap
	 */
	public String getBootstrap() {
		return bootstrap;
	}

	/**
	 * Sets bootstrap.
	 *
	 * @param bootstrap
	 *            the bootstrap
	 * @return the bootstrap
	 */
	public ProviderConfig<T> setBootstrap(String bootstrap) {
		this.bootstrap = bootstrap;
		return this;
	}

	/**
	 * Gets executor.
	 *
	 * @return the executor
	 */
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Sets executor.
	 *
	 * @param executor
	 *            the executor
	 * @return the executor
	 */
	public ProviderConfig<T> setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
		return this;
	}

	/**
	 * Gets concurrents.
	 *
	 * @return the concurrents
	 */
	public int getConcurrents() {
		return concurrents;
	}

	/**
	 * Sets concurrents.
	 *
	 * @param concurrents
	 *            the concurrents
	 * @return the concurrents
	 */
	public ProviderConfig<T> setConcurrents(int concurrents) {
		this.concurrents = concurrents;
		return this;
	}

	/**
	 * Gets client timeout.
	 *
	 * @return the client timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	 /**
     * Gets server.
     *
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets server.
     *
     * @param server the server
     * @return the server
     */
    public ProviderConfig<T> setServer(String server) {
        this.server = server;
        return this;
    }
    
	/**
	 * Sets client timeout.
	 *
	 * @param timeout
	 *            the client timeout
	 * @return the client timeout
	 */
	public ProviderConfig setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * 得到可调用的方法名称列表
	 *
	 * @return 可调用的方法名称列表 methods limit
	 */
	public Map<String, Boolean> getMethodsLimit() {
		return methodsLimit;
	}

	/**
	 * Sets methodsLimit.
	 *
	 * @param methodsLimit
	 *            the methodsLimit
	 * @return the ProviderConfig
	 */
	public ProviderConfig<T> setMethodsLimit(ConcurrentMap<String, Boolean> methodsLimit) {
		this.methodsLimit = methodsLimit;
		return this;
	}

	@Override
	public Class<T> getProxyClass() {
		if (proxyClass != null) {
			return proxyClass;
		}
		try {
			if ((interfaceName != null) && !interfaceName.equals("")) {
				this.proxyClass = ClassUtil.forName(interfaceName);
				if (!proxyClass.isInterface()) {
					throw new RuntimeException(
							"service.interfaceName " + interfaceName +" must set interface class not implement class");
				}
			} else {
				throw new RuntimeException("service.interfaceName is null ,interfaceName must be not null");
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return proxyClass;
	}

	/**
	 * Build key.
	 *
	 * @return the string
	 */
	@Override
	public String buildKey() {
		return interfaceName + ":" + uniqueId;
	}

	@Override
	public boolean hasTimeout() {
		if (timeout > 0) {
			return true;
		}
		if (isNotEmpty(methods)) {
			for (MethodConfig methodConfig : methods.values()) {
				if (methodConfig.getTimeout() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否有并发控制需求，有就打开过滤器 配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
	 *
	 * @return 是否配置了concurrents boolean
	 */
	@Override
	public boolean hasConcurrents() {
		if (concurrents > 0) {
			return true;
		}
		if (isNotEmpty(methods)) {
			for (MethodConfig methodConfig : methods.values()) {
				if (methodConfig.getConcurrents() != null && methodConfig.getConcurrents() > 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void export() {
		List<Filter> filter = FilterChain.loadFilter(getFilter(), false);
		Invoker invoker = ProxyFactory.getInvoker(ref, getProxyClass());
		Invoker chain = FilterChain.buildeFilterChain(invoker, filter);
		
	}
}

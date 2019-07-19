package org.sirius.rpc.registry;

import java.util.concurrent.TimeUnit;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RegistryConfig;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class AbstractRegistry implements Registry {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractRegistry.class);
	public final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("AbstractRegistry.timer", true));
	
	private RegistryConfig config;
	
	public AbstractRegistry(RegistryConfig config) {
		this.config = config;
		init();
	}

	protected RegistryConfig getRegistryConfig() {
		return this.config;
	}
	@Override
	public void register(ProviderConfig config) {

		try {
			doRegister(config);
		} catch (Exception e) {
 
			logger.error("register failed ,wait to retry .." ,e);
			timer.newTimeout(new RegisterRetryTask(config), 3000, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void unRegister(ProviderConfig config) {
		try {
			doUnregister(config);
		} catch (Exception e) {

			logger.error("unRegister failed ,wait to retry .." ,e);
			timer.newTimeout(new UnRegisterRetryTask(config), 3000, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public ConcurrentHashSet<ProviderInfo> subscribe(ConsumerConfig config, ProviderInfoListener listener) {
		try {
			doSubscribe(config, listener);
		} catch (Exception e) {

			logger.error("subscribe failed ,wait to retry .." ,e);
			timer.newTimeout(new SubscribeRetryTask(config,listener), 3000, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	@Override
	public void unSubscribe(ConsumerConfig config) {
		try {
			doUnSubscribe(config);
		} catch (Exception e) {

			logger.error("unSubscribe failed ,wait to retry .." ,e);
			timer.newTimeout(new UnSubscribeRetryTask(config), 3000, TimeUnit.MILLISECONDS);
		}
	}

	
	@Override
	public void start() {

	}

	@Override
	public void shutdown() {

	}
	protected abstract void init();
	protected abstract void doRegister(ProviderConfig config);
	protected abstract void doUnSubscribe(ConsumerConfig config);
	protected abstract void doUnregister(ProviderConfig config) ;
	protected abstract void doSubscribe(ConsumerConfig config, ProviderInfoListener listener);

	public class RegisterRetryTask implements TimerTask {

		private ProviderConfig config;
		public RegisterRetryTask(ProviderConfig config) {
			this.config = config;
		}
		@Override
		public void run(Timeout timeout) throws Exception {
			AbstractRegistry.this.register(config);
		}
	}

	public class UnRegisterRetryTask implements TimerTask {

		private ProviderConfig config;
		public UnRegisterRetryTask(ProviderConfig config) {
			this.config = config;
		}
		@Override
		public void run(Timeout timeout) throws Exception {
			AbstractRegistry.this.unRegister(config);
		}
	}

	public class SubscribeRetryTask implements TimerTask {

		private ConsumerConfig config;
		private ProviderInfoListener listener;
		public SubscribeRetryTask(ConsumerConfig config,ProviderInfoListener listener) {
			this.config = config;
			this.listener = listener;
		}
		
		@Override
		public void run(Timeout timeout) throws Exception {
			AbstractRegistry.this.subscribe(config, listener);
		}
	}
	public class UnSubscribeRetryTask implements TimerTask {

		private ConsumerConfig config;
		public UnSubscribeRetryTask(ConsumerConfig config) {
			this.config = config;
		}
		@Override
		public void run(Timeout timeout) throws Exception {
			AbstractRegistry.this.unSubscribe(config);
		}
	}
}

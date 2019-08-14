package org.sirius.rpc.consumer.loadbalance;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.ext.Extension;
import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.HashUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;

@Extension(value = "consistentHash", singleton = false)
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

	/**
	 * {interface#method : selector}
	 */
	private final ConcurrentHashMap<String, Selector> selectorCache = new ConcurrentHashMap<String, Selector>();

	@Override
	public ChannelGroup doSelect(List<ChannelGroup> ChannelGroups, Request request) {
		String interfaceId = request.getClassName();
		String method = request.getMethodName();
		String key = interfaceId + "#" + method;
		// 判断是否同样的服务列表
		int hashcode = ChannelGroups.hashCode();
		Selector selector = selectorCache.get(key);
		// 原来没有
		if (selector == null ||
		// 或者服务列表已经变化
				selector.getHashCode() != hashcode) {
			selector = new Selector(interfaceId, method, ChannelGroups, hashcode);
			selectorCache.put(key, selector);
		}
		return selector.select(request);
	}

	/**
	 * 选择器
	 */
	private static class Selector {

		/**
		 * The Hashcode.
		 */
		private final int hashcode;

		/**
		 * The Interface id.
		 */
		private final String interfaceId;

		/**
		 * The Method name.
		 */
		private final String method;

		/**
		 * 虚拟节点
		 */
		private final TreeMap<Long, ChannelGroup> virtualNodes;

		/**
		 * Instantiates a new Selector.
		 *
		 * @param interfaceId
		 *            the interface id
		 * @param method
		 *            the method
		 * @param actualNodes
		 *            the actual nodes
		 * @param hashcode
		 *            the hashcode
		 */
		public Selector(String interfaceId, String method, List<ChannelGroup> actualNodes, int hashcode) {
			this.interfaceId = interfaceId;
			this.method = method;
			this.hashcode = hashcode;
			// 创建虚拟节点环 （provider创建虚拟节点数 = 真实节点权重 * 32）
			this.virtualNodes = new TreeMap<Long, ChannelGroup>();
			// 设置越大越慢，精度越高
			int num = 32;
			for (ChannelGroup ChannelGroup : actualNodes) {
				for (int i = 0; i < num * ChannelGroup.getWeight() / 4; i++) {
					byte[] digest = HashUtils.messageDigest(
							ChannelGroup.remoteAddress().getHost() + ChannelGroup.remoteAddress().getPort() + i);
					for (int h = 0; h < 4; h++) {
						long m = HashUtils.hash(digest, h);
						virtualNodes.put(m, ChannelGroup);
					}
				}
			}
		}

		/**
		 * Select provider.
		 *
		 * @param request
		 *            the request
		 * @return the provider
		 */
		public ChannelGroup select(Request request) {
			String key = buildKeyOfHash(request.getParameters());
			byte[] digest = HashUtils.messageDigest(key);
			return selectForKey(HashUtils.hash(digest, 0));
		}

		/**
		 * 获取第一参数作为hash的key
		 *
		 * @param args
		 *            the args
		 * @return the string
		 */
		private String buildKeyOfHash(Object[] args) {
			if (CommonUtils.isEmpty(args)) {
				return StringUtils.EMPTY;
			} else {
				return StringUtils.toString(args[0]);
			}
		}

		/**
		 * Select for key.
		 *
		 * @param hash
		 *            the hash
		 * @return the provider
		 */
		private ChannelGroup selectForKey(long hash) {
			Map.Entry<Long, ChannelGroup> entry = virtualNodes.ceilingEntry(hash);
			if (entry == null) {
				entry = virtualNodes.firstEntry();
			}
			return entry.getValue();
		}

		/**
		 * Gets hash code.
		 *
		 * @return the hash code
		 */
		public int getHashCode() {
			return hashcode;
		}
	}

}

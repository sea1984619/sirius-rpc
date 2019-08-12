package org.sirius.rpc.consumer.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sirius.common.util.StringUtils;
import org.sirius.common.util.ThrowUtil;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import com.google.common.collect.Maps;

/*
 *规则用法和dubbo一样
 */
public class ConditionRouter implements Router {

	private static final String middle_separator = "=>";
	private static final String host = "host";
	private static final String method = "method";
	private static final String application = "application";

	private String condition;
	private ConsumerConfig<?> config;
	public List<Condition> leftConditions = new ArrayList<Condition>();
	public List<Condition> rightConditions = new ArrayList<Condition>();

	public ConditionRouter(String condition, ConsumerConfig<?> config) {
		this.config = config;
		this.condition = condition.replaceAll(" ", "");
		String[] pair = StringUtils.split(this.condition, middle_separator);
		String[] lefts = StringUtils.split(pair[0], "&");
		for (String left : lefts) {
			if (!StringUtils.isEmpty(left)) {
				Condition c = new Condition(left);
				boolean a = c.available ? leftConditions.add(c) : null;
			}
		}
		String[] rights = StringUtils.split(pair[1], "&");
		for (String right : rights) {
			if (!StringUtils.isEmpty(right)) {
				Condition c = new Condition(right);
				boolean a = c.available ? rightConditions.add(c) : null;
			}
		}
	}

	@Override
	public List<ChannelGroup> route(List<ChannelGroup> groupList, Request request) {
		boolean needFilte = false;
		if()
		String kind = leftCondition.getKind();
		if (kind == null) {
			return groupList;
		}
		switch (kind) {
		case host:
			String host = groupList.get(0).localAddress().getHost();
			needFilte = handle(host);
			break;
		case method:
			String methodName = request.getMethodName();
			needFilte = handle(methodName);
			break;
		case application:
			String appName = config.getAppName();
			needFilte = handle(appName);
			break;
		}
		if (needFilte) {
			List<ChannelGroup> filted = new ArrayList<ChannelGroup>();
			if (rightCondition.nullConditon)
				return filted;
			if (groupList.size() == 0)
				return groupList;
			for (ChannelGroup group : groupList) {
				String remoteAddress = group.remoteAddress().getHost();
				if (rightCondition.getInclude() != null) {
					if (rightCondition.matchInclude(remoteAddress)) {
						filted.add(group);
						System.out.println("a:" + group.remoteAddress());
					}
				} else {
					if (!rightCondition.matchExclude(remoteAddress)) {
						filted.add(group);
						System.out.println("b:" + group.remoteAddress());
					}
				}
			}
			return filted;
		}
		return groupList;
	}

	private boolean handle(String tobeMatched) {
		if (leftCondition.getInclude() != null) {
			if (leftCondition.matchInclude(tobeMatched)) {
				return true;
			}
		} else {
			if (leftCondition.getExclude() != null) {
				if (!leftCondition.matchExclude(tobeMatched))
					return true;
			}
		}
		return false;
	}

	public String getCondition() {
		return this.condition;
	}

	public class Condition {

		private static final String equal = "=";
		private static final String not_equal = "!=";
		private static final String wildcard = "*";
		private String kind;
		private List<String> include;
		private List<String> exclude;
		public boolean available = false;

		public Condition(String condition) {
			if (condition.indexOf(not_equal) > 0) {
				String[] pair = StringUtils.split(condition, not_equal);
				String kind = pair[0];
				String expression = pair[1];
				if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(expression)) {
					ThrowUtil.throwException(new RpcException("the expression of [" + condition
							+ "] is wrong ,both side of (= or != ) must not be blank"));
				}
				this.kind = kind;
				exclude = Arrays.asList(StringUtils.splitWithCommaOrSemicolon(expression));
				this.available = true;
				return;
			} else {
				if (condition.indexOf(equal) > 0) {
					String[] pair = StringUtils.split(condition, equal);
					String kind = pair[0];
					String expression = pair[1];
					if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(expression)) {
						ThrowUtil.throwException(new RpcException("the expression of [" + condition
								+ "] is wrong ,both side of (= or != ) must not be blank"));
					}
					this.kind = kind;
					include = Arrays.asList(StringUtils.splitWithCommaOrSemicolon(expression));
					this.available = true;
					return;
				}
			}
			ThrowUtil.throwException(
					new RpcException("the expression of [" + condition + "] is wrong , must contain = or != "));
		}

		public boolean matchInclude(String tobematched) {
			for (String condition : include) {
				if (match(tobematched, condition)) {
					return true;
				}
			}
			return false;
		}

		public boolean matchExclude(String tobematched) {
			for (String condition : exclude) {
				if (match(tobematched, condition)) {
					return true;
				}
			}
			return false;
		}

		public boolean match(String tobematched, String condition) {
			if (tobematched.equals(condition))
				return true;
			if (condition.equals(wildcard))
				return true;
			if (condition.endsWith(wildcard)) {
				String sub = condition.substring(0, condition.length() - 1);
				if (tobematched.startsWith(sub))
					return true;
			}
			return false;
		}

		public List<String> getInclude() {
			return include;
		}

		public List<String> getExclude() {
			return exclude;
		}

		public String getKind() {
			return kind;
		}
	}

	@Override
	public boolean equals(Object that) {
		if (that.equals(this))
			return true;
		if (that == null || that.getClass() != this.getClass())
			return false;
		ConditionRouter c = (ConditionRouter) that;
		return c.getCondition().equals(this.getCondition());
	}

	@Override
	public int hashCode() {
		return this.getCondition().hashCode();
	}

	public static void main(String args[]) {

		String rule = "method!=get*=> host= 192.168.1.1";
		ChannelGroup group1 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.1.1", 2000));
		group1.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));
		ChannelGroup group2 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.1.2", 2000));
		group2.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));
		ChannelGroup group3 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.1.3", 2000));
		group3.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));
		ChannelGroup group4 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.1.4", 2000));
		group4.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));

		List<ChannelGroup> list = new ArrayList<ChannelGroup>();
		list.add(group1);
		list.add(group2);
		list.add(group3);
		list.add(group4);

		Request r = new Request();
		r.setMethodName("getApple");
		ConditionRouter cr = new ConditionRouter(rule, null);

		List filted = cr.route(list, r);
		System.out.println(filted.size());

	}

}

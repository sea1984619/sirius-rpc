package org.sirius.rpc.consumer.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sirius.common.util.StringUtils;
import org.sirius.common.util.ThrowUtil;
import org.sirius.rpc.RpcException;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.channel.NettyChannelGroup;

/*
 *规则用法和dubbo一样
 */
public class ConditionRouter implements Router {

	private static final String middle_separator = "=>";
	private static final String host = "host";
	private static final String method = "method";

	private String condition;
	private ConsumerConfig<?> config;
	/*
	 * 左匹配支持多个条件. 以"&"分割 例如 "host = 192.* & method = get*"
	 */
	public List<Condition> leftConditions = new ArrayList<Condition>();
	/*
	 * 右匹配暂时只支持 host一个条件匹配。
	 */
	public Condition rightCondition;

	public ConditionRouter(String condition, ConsumerConfig<?> config) {
		this.config = config;
		this.condition = condition.replaceAll(" ", "");
		if (condition.indexOf(middle_separator) < 0)
			ThrowUtil.throwException(
					new RpcException("the expression of [" + condition + "] is wrong ,must contain =>"));
		String[] pair = StringUtils.split(this.condition, middle_separator);
		String[] lefts = StringUtils.split(pair[0], "&");
		for (String left : lefts) {
			if (!StringUtils.isEmpty(left)) {
				leftConditions.add(new Condition(left));
			}
		}
		if (!StringUtils.isEmpty(pair[1]))
			rightCondition = new Condition(pair[1]);
	}

	@Override
	public List<ChannelGroup> route(List<ChannelGroup> groupList, Request request) {
		boolean needFilte = false;
		if (leftConditions.size() == 0) {
			needFilte = true;
		} else {
			for (Condition c : leftConditions) {
				String kind = c.getKind();
				String tobeMatched;
				if (kind.equals(host))
					tobeMatched = groupList.get(0).localAddress().getHost();
				else if (kind.equals(method))
					tobeMatched = request.getMethodName();
				else
					tobeMatched = config.getConfigValueCache().get(kind).toString();

				/*
				 * 条件需要全部匹配,有一个不匹配就不需要过滤了
				 */
				if (!c.match(tobeMatched)) {
					needFilte = false;
					break;
				}
				needFilte = true;
			}
		}
		if (needFilte) {
			List<ChannelGroup> filted = new ArrayList<ChannelGroup>();

			if (groupList.size() == 0 || rightCondition == null)
				return filted;

			for (ChannelGroup group : groupList) {
				String remoteAddress = group.remoteAddress().getHost();
				if (rightCondition.match(remoteAddress)) {
					filted.add(group);
					System.out.println("select : " + group.remoteAddress());
				}
			}
			return filted;
		}
		return groupList;
	}

	public String getCondition() {
		return this.condition;
	}

	private class Condition {

		private static final String equal = "=";
		private static final String not_equal = "!=";
		private static final String wildcard = "*";
		private String kind;
		private boolean isExclude;
		private List<String> conditions;

		Condition(String condition) {
			String[] pair = null;
			if (condition.indexOf(equal) < 0) {
				ThrowUtil.throwException(
						new RpcException("the expression of [" + condition + "] is wrong ,must contain = or !="));
			}
			if (condition.indexOf(not_equal) > 0) {
				pair = StringUtils.split(condition, not_equal);
				this.isExclude = true;
			} else {
				pair = StringUtils.split(condition, equal);
			}
			if (StringUtils.isEmpty(pair[0]) || StringUtils.isEmpty(pair[1])) {
				ThrowUtil.throwException(new RpcException(
						"the expression of [" + condition + "] is wrong ,both side of (= or != ) must not be blank"));
			}
			this.kind = pair[0];
			conditions = Arrays.asList(StringUtils.splitWithCommaOrSemicolon(pair[1]));
		}

		boolean match(String tobeMatched) {
			boolean matched = false;
			for (String condition : conditions) {
				if (match(tobeMatched, condition)) {
					matched = true;
					break;
				}
			}
			if (isExclude)
				matched = !matched;
			return matched;
		}

		/*
		 * 正则表达式不会搞,所以,很简单的匹配方法,不过可以满足绝大部分需求了。
		 */
		private boolean match(String tobeMatched, String condition) {
			if (tobeMatched.equals(condition))
				return true;
			if (condition.equals(wildcard))
				return true;
			if (condition.endsWith(wildcard)) {
				String sub = condition.substring(0, condition.length() - 1);
				if (tobeMatched.startsWith(sub))
					return true;
			}
			return false;
		}

		String getKind() {
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

		String rule = "host!=192.168.* & method= get*=>  host!= 192.168.1.*";
		ChannelGroup group1 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.1.1", 2000));
		group1.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));
		ChannelGroup group2 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.3.2", 2000));
		group2.setLocalAddress(new UnresolvedSocketAddress("192.168.1.10", 2000));
		ChannelGroup group3 = new NettyChannelGroup(new UnresolvedSocketAddress("192.168.5.3", 2000));
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

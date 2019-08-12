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
 *规则用法和dubbo一样, 每一个ConditionRouter只过滤一类条件,
 *比如 host=192.168.12.0,152.200.133.12 => host=192.168.1.1
 *     method=get* => host=192.168.1.1
 */
public class ConditionRouter implements Router {

	private static final String middle_separator = "=>";
	private static final String wildcard = "*";
	private static final String host = "host";
	private static final String method = "method";
	private static final String application = "application";

	private String condition;
	private ConsumerConfig config;
	public Condition leftCondition;
	public Condition rightCondition;

	public ConditionRouter(String condition, ConsumerConfig config) {
		this.condition = condition.replaceAll(" ", "");
		this.config = config;
		String[] pair = StringUtils.split(this.condition, middle_separator);
		this.leftCondition = new Condition(pair[0]);
		this.rightCondition = new Condition(pair[1]);

	}

	@Override
	public List<ChannelGroup> route(List<ChannelGroup> groupList, Request request) {

		String kind = leftCondition.getKind();
		if (kind == null) {
			return groupList;
		}
		boolean needFilte = false;
		switch(kind){
		case host :
			String host = groupList.get(0).localAddress().getHost();
			needFilte = handle(host);
			break;
		case method :
			String methodName = request.getMethodName();
			needFilte = handle(methodName);
			break;
		case application :
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
						System.out.println("a:"+group.remoteAddress());
					}
				} else {
					if (!rightCondition.matchExclude(remoteAddress)) {
						filted.add(group);
						System.out.println("b:"+group.remoteAddress());
					}
				}
			}
			return filted;
		}
		return groupList;
	}

	private boolean handle(String  tobeMatched) {
		if(leftCondition.getInclude() != null) {
			if (leftCondition.matchInclude(tobeMatched)) {
				return true;
			}
		}else {
			if(leftCondition.getExclude() != null) {
				if(!leftCondition.matchExclude(tobeMatched))
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
		private boolean nullConditon;
		private String kind;
		private List<String> include;
		private List<String> exclude;

		public Condition(String condition) {
			if (StringUtils.isEmpty(condition)) {
				nullConditon = true;
				return;
			}
			if (condition.indexOf(not_equal) > 0) {
				String[] pair = StringUtils.split(condition, not_equal);
				String kind = pair[0];
				String expression = pair[1];
				if (StringUtils.isEmpty(kind)) {
					return;// 无效规则,直接返回
				}
				this.kind = kind;
				exclude = Arrays.asList(StringUtils.splitWithCommaOrSemicolon(expression));
			} else {
				if (condition.indexOf(equal) > 0) {
					String[] pair = StringUtils.split(condition, equal);
					String kind = pair[0];
					String expression = pair[1];
					if (StringUtils.isEmpty(kind)) {
						return; // 无效规则,直接返回
					}
					this.kind = kind;
					include = Arrays.asList(StringUtils.splitWithCommaOrSemicolon(expression));
				}
			}
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

		public boolean isNullConditon() {
			return nullConditon;
		}

		public void setNullConditon(boolean nullConditon) {
			this.nullConditon = nullConditon;
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

package org.sirius.transport.api;

/**
 * 服务目录: <服务组别, 服务名称, 服务版本号>
 */
public abstract class Directory {

	private String group;
	private String serviceName;
	private String vision;
	
	abstract String group();
	abstract String serviceName();
	abstract String vision();
	
	public boolean equals(Object o) {
		if(o==this) return true;
		if(o==null||o.getClass()!=this.getClass()) return false;
		Directory that = (Directory)o;
		return that.group().equals(this.group)
				 &&that.serviceName().equals(this.serviceName())
				   &&that.vision().equals(this.vision());
	}
	
}

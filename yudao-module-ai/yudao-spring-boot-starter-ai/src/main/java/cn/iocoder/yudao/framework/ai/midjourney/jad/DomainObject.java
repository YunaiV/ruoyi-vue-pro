package cn.iocoder.yudao.framework.ai.midjourney.jad;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class DomainObject implements Serializable {
	@Getter
	@Setter
	protected String id;

	@Setter
	protected Map<String, Object> properties; // 扩展属性，仅支持基本类型

	@JsonIgnore
	private final transient Object lock = new Object();

	public void sleep() throws InterruptedException {
		synchronized (this.lock) {
			this.lock.wait();
		}
	}

	public void awake() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

	public DomainObject setProperty(String name, Object value) {
		getProperties().put(name, value);
		return this;
	}

	public DomainObject removeProperty(String name) {
		getProperties().remove(name);
		return this;
	}

	public Object getProperty(String name) {
		return getProperties().get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getPropertyGeneric(String name) {
		return (T) getProperty(name);
	}

	public <T> T getProperty(String name, Class<T> clz) {
		return getProperty(name, clz, null);
	}

	public <T> T getProperty(String name, Class<T> clz, T defaultValue) {
		Object value = getProperty(name);
		return value == null ? defaultValue : clz.cast(value);
	}

	public Map<String, Object> getProperties() {
		if (this.properties == null) {
			this.properties = new HashMap<>();
		}
		return this.properties;
	}
}

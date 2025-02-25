package cn.iocoder.yudao.framework.common.util.cache;


import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 变量，用于包装值，可设置值有效期
 * */
public class Variable {

	/**
	 * 超时模式
	 * @author leefangjie
	 * */
	public static enum ExpireType {
		/**
		 * 按空闲时长记超时时长，存取操作都会延长超时时间
		 * */
		IDLE,
		/**
		 * 按存活时间即超时时长，生成后超时时间不再变化
		 * */
		LIVE;
	}

 
	private ExpireType expireType=ExpireType.LIVE;
	
	/**
	 * 获得超时类型，默认类型 ExpireType.LIVE
	 * @param  expireType ExpireType 枚举中的类型；
	 * */
	public void setExpireType(ExpireType expireType) {
		if(!isExpire()) {
			this.time=System.currentTimeMillis();
		}
		this.expireType = expireType;
		
	}
	
	/**
	 * 获得超时类型，默认类型 ExpireType.LIVE
	 * @return ExpireType
	 * */
	public ExpireType getExpireType() {
		return expireType;
	}

	private Object value=null;
	
	/**
	 * 是否过期
	 * @return 是否过期
	 * */
	public boolean isExpire() {
		boolean flag = expire >0 && System.currentTimeMillis()-time>expire;
		if(flag){
			// 有利于缓存回收
			this.value=null;
		}
		return flag;
	}
	/**
	 * 获取值，如果超时，返回 null
	 * @return 值
	 * */
	public Object getValue() {
		if(isExpire()) {
			return null;
		} else {
			if(expireType==ExpireType.IDLE) {
				this.time=System.currentTimeMillis();
			}
			return value;
		}
	}
	/**
	 * 设置值
	 * @param value 值
	 * */
	public void setValue(Object value) {
		this.value = value;
		this.time=System.currentTimeMillis();
	}
	
	private long expire=0;

	/**
	 * @return 超时时长，毫秒
	 * */
	public long getExpire() {
		return expire;
	}

	/**
	 * @param expire  超时时间，单位毫秒
	 */
	public void setExpire(long expire) {
		if(expireType==ExpireType.IDLE) {
			this.time=System.currentTimeMillis();
		}
		this.expire = expire;
	}
	
	private long time=0;

	/**
	 * 获得最后一次设置值的时间戳
	 * @return 时间戳
	 * */
	public long getLastSetTime() {
		return time;
	}
	
	/**
	 * @param value 值
	 * @param  expire 超时时间，单位毫秒
	 */
	public Variable(Object value, int expire)
	{
		this.setValue(value);
		this.setExpire(expire);
	}
	
	/**
	 * @param value 值
	 * @param  expire 超时时间，单位毫秒
	 * @param expireType 超时类型
	 */
	public Variable(Object value, int expire, ExpireType expireType)
	{
		this.setValue(value);
		this.setExpire(expire);
		this.setExpireType(expireType);
	}
	
	/**
	 * @param value 值
	 * */
	public Variable(Object value)
	{
		this.setValue(value);
	}
	
	/**
	 * @return 字符串值
	 * */
	public String stringValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof String) {
			return (String) value;
		}
		return value.toString();
	}
	
	/**
	 * @return 整型值
	 * */
	public Integer integerValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof Integer) {
			return (Integer) value;
		}
		try {
			return Integer.valueOf(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return 短整型值
	 * */
	public Short shortValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof Short) {
			return (Short) value;
		}
		try {
			return Short.valueOf(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return 长整型值
	 * */
	public Long longValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof Long) {
			return (Long) value;
		}
		try {
			return Long.valueOf(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return 浮点型值
	 * */
	public Float floatValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof Float) {
			return (Float) value;
		}
		try {
			return Float.valueOf(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return 双精度型值
	 * */
	public Double doubleValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof Double) {
			return (Double) value;
		}
		try {
			return Double.valueOf(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return BigDecimal型值
	 * */
	public BigDecimal bigDecimalValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		try {
			return new BigDecimal(stringValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return BigInteger型值
	 * */
	public BigInteger bigIntegerValue()
	{
		Object value=getValue();
		if (value==null) {
			return null;
		}
		if(value instanceof BigInteger) {
			return (BigInteger) value;
		}
		try {
			return new BigInteger(stringValue());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		if(this.name!=null) {
			return this.name+" = " + this.value;
		} else {
			return this.value+"";
		}
		
	}
	
	private String name=null;
	
	/**
	 * 获得变量名称
	 * @return 名称
	 * */
	public String getName() {
		return name;
	}

	/**
	 * 设置变量名称
	 * @param name 名称
	 * */
	public void setName(String name) {
		this.name = name;
	}
	
}

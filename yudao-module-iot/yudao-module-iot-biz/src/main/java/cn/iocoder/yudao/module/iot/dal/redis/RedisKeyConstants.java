package cn.iocoder.yudao.module.iot.dal.redis;



/**
 * Iot Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 设备属性数据缓存
     * <p>
     * KEY 格式：device_property_data:{deviceId}
     * VALUE 数据类型：String 设备属性数据
     */
    String DEVICE_PROPERTY_DATA = "device_property_data:%s_%s_%s";

}

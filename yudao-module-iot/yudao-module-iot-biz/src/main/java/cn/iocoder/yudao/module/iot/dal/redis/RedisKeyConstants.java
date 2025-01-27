package cn.iocoder.yudao.module.iot.dal.redis;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;

/**
 * Iot Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 设备属性数据缓存，采用 HASH 结构
     * <p>
     * KEY 格式：device_property:{deviceKey}
     * HASH KEY：identifier 属性标识
     * VALUE 数据类型：String(JSON) {@link IotDevicePropertyDO}
     */
    String DEVICE_PROPERTY = "device_property:%s";

    /**
     * 设备的最后上报时间，采用 ZSET 结构
     *
     * KEY 格式：{deviceKey}
     * SCORE：上报时间
     */
    String DEVICE_REPORT_TIME = "device_report_time";

}

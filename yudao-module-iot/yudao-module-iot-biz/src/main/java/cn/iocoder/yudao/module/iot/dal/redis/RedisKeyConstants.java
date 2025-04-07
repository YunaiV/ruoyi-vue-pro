package cn.iocoder.yudao.module.iot.dal.redis;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;

/**
 * IoT Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 设备属性的数据缓存，采用 HASH 结构
     * <p>
     * KEY 格式：device_property:{deviceKey}
     * HASH KEY：identifier 属性标识
     * VALUE 数据类型：String(JSON) {@link IotDevicePropertyDO}
     */
    String DEVICE_PROPERTY = "iot:device_property:%s";

    /**
     * 设备的最后上报时间，采用 ZSET 结构
     *
     * KEY 格式：{deviceKey}
     * SCORE：上报时间
     */
    String DEVICE_REPORT_TIMES = "iot:device_report_times";

    /**
     * 设备信息的数据缓存，使用 Spring Cache 操作（忽略租户）
     *
     * KEY 格式：device_${productKey}_${deviceKey}
     * VALUE 数据类型：String(JSON)
     */
    String DEVICE = "iot:device";

    /**
     * 物模型的数据缓存，使用 Spring Cache 操作（忽略租户）
     *
     * KEY 格式：thing_model_${productKey}
     * VALUE 数据类型：String 数组(JSON)，即 {@link cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO} 列表
     */
    String THING_MODEL_LIST = "iot:thing_model_list";

    /**
     * 设备插件的插件进程编号的映射，采用 HASH 结构
     *
     * KEY 格式：device_plugin_instance_process_ids
     * HASH KEY：${deviceKey}
     * VALUE：插件进程编号，对应 {@link IotPluginInstanceDO#getProcessId()} 字段
     */
    String DEVICE_PLUGIN_INSTANCE_PROCESS_IDS = "iot:device_plugin_instance_process_ids";

}

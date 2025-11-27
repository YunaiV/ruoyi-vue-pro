package cn.iocoder.yudao.module.iot.dal.redis;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;

/**
 * IoT Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 设备属性的数据缓存，采用 HASH 结构
     * <p>
     * KEY 格式：device_property:{deviceId}
     * HASH KEY：identifier 属性标识
     * VALUE 数据类型：String(JSON) {@link IotDevicePropertyDO}
     */
    String DEVICE_PROPERTY = "iot:device_property:%d";

    /**
     * 设备的最后上报时间，采用 ZSET 结构
     *
     * KEY 格式：{deviceId}
     * SCORE：上报时间
     */
    String DEVICE_REPORT_TIMES = "iot:device_report_times";

    /**
     * 设备关联的网关 serverId 缓存，采用 HASH 结构
     *
     * KEY 格式：device_server_id
     * HASH KEY：{deviceId}
     * VALUE 数据类型：String serverId
     */
    String DEVICE_SERVER_ID = "iot:device_server_id";

    /**
     * 设备信息的数据缓存，使用 Spring Cache 操作（忽略租户）
     *
     * KEY 格式 1：device_${deviceId}
     * KEY 格式 2：device_${productKey}_${deviceName}
     * VALUE 数据类型：String(JSON)
     */
    String DEVICE = "iot:device";

    /**
     * 产品信息的数据缓存，使用 Spring Cache 操作（忽略租户）
     *
     * KEY 格式：product_${productId}
     * VALUE 数据类型：String(JSON)
     */
    String PRODUCT = "iot:product";

    /**
     * 物模型的数据缓存，使用 Spring Cache 操作（忽略租户）
     *
     * KEY 格式：thing_model_${productId}
     * VALUE 数据类型：String 数组(JSON)，即 {@link cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO} 列表
     */
    String THING_MODEL_LIST = "iot:thing_model_list";

    /**
     * 数据流转规则的数据缓存，使用 Spring Cache 操作
     *
     * KEY 格式：data_rule_list_${deviceId}_${method}_${identifier}
     * VALUE 数据类型：String 数组(JSON)，即 {@link cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO} 列表
     */
    String DATA_RULE_LIST = "iot:data_rule_list";

    /**
     * 数据目的的数据缓存，使用 Spring Cache 操作
     *
     * KEY 格式：data_sink_${id}
     * VALUE 数据类型：String(JSON)，即 {@link cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO}
     */
    String DATA_SINK = "iot:data_sink";

    /**
     * 场景联动规则的数据缓存，使用 Spring Cache 操作
     * <p>
     * KEY 格式：scene_rule_list_${productId}_${deviceId}
     * VALUE 数据类型：String 数组(JSON)，即 {@link cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO} 列表
     */
    String SCENE_RULE_LIST = "iot:scene_rule_list";

}

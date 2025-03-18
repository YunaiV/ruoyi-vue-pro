package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 执行设备控制
 *
 * @author 芋道源码
 */
@Data
public class IotRuleSceneActionDeviceControl {

    /**
     * 产品标识
     *
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备名称数组
     *
     * 关联 {@link IotDeviceDO#getDeviceName()}
     */
    private List<String> deviceNames;

    /**
     * 消息类型
     *
     * 枚举 {@link IotDeviceMessageTypeEnum#PROPERTY}、{@link IotDeviceMessageTypeEnum#SERVICE}
     */
    private String type;
    /**
     * 消息标识符
     *
     * 枚举 {@link IotDeviceMessageIdentifierEnum}
     *
     * 1. 属性设置：对应 {@link IotDeviceMessageIdentifierEnum#PROPERTY_SET}
     * 2. 服务调用：对应 {@link IotDeviceMessageIdentifierEnum#SERVICE_INVOKE}
     */
    private String identifier;

    /**
     * 具体数据
     *
     * 1. 属性设置：在 {@link #type} 是 {@link IotDeviceMessageTypeEnum#PROPERTY} 时，对应 properties
     * 2. 服务调用：在 {@link #type} 是 {@link IotDeviceMessageTypeEnum#SERVICE} 时，对应 params
     */
    private Map<String, Object> data;

}
package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备日志数据 DO
 *
 * 目前使用 TDengine 存储
 *
 * @author alwayssuper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceLogDO {

    /**
     * 日志编号
     *
     * 通过 {@link IdUtil#fastSimpleUUID()} 生成
     */
    private String id;

    /**
     * 消息编号
     *
     * 对应 {@link IotDeviceMessage#getMessageId()} 字段
     */
    private String messageId;

    /**
     * 产品标识
     * <p>
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备名称
     *
     * 关联 {@link IotDeviceDO#getDeviceName()}
     */
    private String deviceName;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * 日志类型
     *
     * 枚举 {@link IotDeviceMessageTypeEnum}
     */
    private String type;
    /**
     * 标识符
     *
     * 枚举 {@link IotDeviceMessageIdentifierEnum}
     */
    private String identifier;

    /**
     * 数据内容
     *
     * 存储具体的消息数据内容，通常是 JSON 格式
     */
    private String content;
    /**
     * 响应码
     *
     * 目前只有 server 下行消息给 device 设备时，才会有响应码
     */
    private Integer code;

    /**
     * 上报时间戳
     */
    private Long reportTime;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 时序时间
     */
    private Long ts;

}

package cn.iocoder.yudao.module.iot.mq.message;

import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// TODO @芋艿：参考阿里云的物模型，优化 IoT 上下行消息的设计，尽量保持一致（渐进式，不要一口气）！
/**
 * IoT 设备消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotDeviceMessage {

    /**
     * 请求编号
     */
    private String requestId;

    /**
     * 设备信息
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备标识
     */
    private String deviceKey;

    /**
     * 消息类型
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
     * 请求参数
     *
     * 例如说：属性上报的 properties、事件上报的 params
     */
    private Object data;
    /**
     * 响应码
     *
     * 目前只有 server 下行消息给 device 设备时，才会有响应码
     */
    private Integer code;

    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}

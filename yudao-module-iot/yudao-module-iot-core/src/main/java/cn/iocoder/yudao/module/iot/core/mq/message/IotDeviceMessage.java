package cn.iocoder.yudao.module.iot.core.mq.message;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotDeviceMessage {

    /**
     * 【消息总线】应用的设备消息 Topic，由 iot-gateway 发给 iot-biz 进行消费
     */
    public static final String MESSAGE_BUS_DEVICE_MESSAGE_TOPIC = "iot_device_message";

    /**
     * 【消息总线】设备消息 Topic，由 iot-biz 发送给 iot-gateway 的某个 “server”(protocol) 进行消费
     *
     * 其中，%s 就是该“server”(protocol) 的标识
     */
    public static final String MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC = MESSAGE_BUS_DEVICE_MESSAGE_TOPIC + "_%s";

    /**
     * 消息编号
     */
    private String messageId;

    /**
     * 设备信息
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;

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
    // TODO @芋艿：可能会去掉
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
     * 服务编号，该消息由哪个 server 服务进行消费
     */
    private String serverId;

    public IotDeviceMessage ofPropertyReport(Map<String, Object> properties) {
        this.setType(IotDeviceMessageTypeEnum.PROPERTY.getType());
        this.setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier());
        this.setData(properties);
        return this;
    }

    public IotDeviceMessage ofPropertySet(Map<String, Object> properties) {
        this.setType(IotDeviceMessageTypeEnum.PROPERTY.getType());
        this.setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_SET.getIdentifier());
        this.setData(properties);
        return this;
    }

    public IotDeviceMessage ofStateOnline() {
        this.setType(IotDeviceMessageTypeEnum.STATE.getType());
        this.setIdentifier(IotDeviceMessageIdentifierEnum.STATE_ONLINE.getIdentifier());
        return this;
    }

    public IotDeviceMessage ofStateOffline() {
        this.setType(IotDeviceMessageTypeEnum.STATE.getType());
        this.setIdentifier(IotDeviceMessageIdentifierEnum.STATE_OFFLINE.getIdentifier());
        return this;
    }

    public static IotDeviceMessage of(String productKey, String deviceName) {
        return of(productKey, deviceName,
                null, null);
    }

    public static IotDeviceMessage of(String productKey, String deviceName,
                                      String serverId) {
        return of(productKey, deviceName,
                null, serverId);
    }

    public static IotDeviceMessage of(String productKey, String deviceName,
                                      LocalDateTime reportTime, String serverId) {
        if (reportTime == null) {
            reportTime = LocalDateTime.now();
        }
        String messageId = IotDeviceMessageUtils.generateMessageId();
        return IotDeviceMessage.builder()
                .messageId(messageId).reportTime(reportTime)
                .productKey(productKey).deviceName(deviceName)
                .serverId(serverId).build();
    }

}

package cn.iocoder.yudao.module.iot.core.mq.message;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     *
     * 由后端生成，通过 {@link IotDeviceMessageUtils#generateMessageId()}
     */
    private String id;
    /**
     * 上报时间
     *
     * 由后端生成，当前时间
     */
    private LocalDateTime reportTime;

    // ========== codec（编解码）字段 ==========

    /**
     * 请求编号
     *
     * 由设备生成，对应阿里云 IoT 的 Alink 协议中的 id、华为云 IoTDA 协议的 request_id
     */
    private String requestId;
    /**
     * 请求方法
     *
     * 枚举 {@link IotDeviceMessageMethodEnum}
     * 例如说：thing.property.report 属性上报
     */
    private String method;
    /**
     * 请求参数
     *
     * 例如说：属性上报的 properties、事件上报的 params
     */
    private Object params;
    /**
     * 响应结果
     */
    private Object data;
    /**
     * 响应错误码
     */
    private Integer code;

    // ========== 后端字段 ==========

    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 服务编号，该消息由哪个 server 服务进行消费
     */
    private String serverId;

//    public IotDeviceMessage ofPropertyReport(Map<String, Object> properties) {
//        this.setType(IotDeviceMessageTypeEnum.PROPERTY.getType());
//        this.setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier());
//        this.setData(properties);
//        return this;
//    }
//
//    public IotDeviceMessage ofPropertySet(Map<String, Object> properties) {
//        this.setType(IotDeviceMessageTypeEnum.PROPERTY.getType());
//        this.setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_SET.getIdentifier());
//        this.setData(properties);
//        return this;
//    }
//
//    public IotDeviceMessage ofStateOnline() {
//        this.setType(IotDeviceMessageTypeEnum.STATE.getType());
//        this.setIdentifier(IotDeviceMessageIdentifierEnum.STATE_ONLINE.getIdentifier());
//        return this;
//    }
//
//    public IotDeviceMessage ofStateOffline() {
//        this.setType(IotDeviceMessageTypeEnum.STATE.getType());
//        this.setIdentifier(IotDeviceMessageIdentifierEnum.STATE_OFFLINE.getIdentifier());
//        return this;
//    }
//
//    public static IotDeviceMessage of(String productKey, String deviceName) {
//        return of(productKey, deviceName,
//                null, null);
//    }
//
//    public static IotDeviceMessage of(String productKey, String deviceName,
//                                      String serverId) {
//        return of(productKey, deviceName,
//                null, serverId);
//    }
//
//    public static IotDeviceMessage of(String productKey, String deviceName,
//                                      LocalDateTime reportTime, String serverId) {
//        if (reportTime == null) {
//            reportTime = LocalDateTime.now();
//        }
//        String messageId = IotDeviceMessageUtils.generateMessageId();
//        return IotDeviceMessage.builder()
//                .messageId(messageId).reportTime(reportTime)
//                .productKey(productKey).deviceName(deviceName)
//                .serverId(serverId).build();
//    }

    public static IotDeviceMessage of(String requestId, String method, Object params) {
        return of(requestId, method, params, null, null);
    }

    public static IotDeviceMessage of(String requestId, String method,
                                      Object params, Object data, Integer code) {
        // 通用参数
        IotDeviceMessage message = new IotDeviceMessage()
                .setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now());
        // 当前参数
        message.setRequestId(requestId).setMethod(method).setParams(params).setData(data).setCode(code);
        return message;
    }

}

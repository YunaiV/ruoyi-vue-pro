package cn.iocoder.yudao.module.iot.core.mq.message;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
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
     * 【消息总线】设备消息 Topic，由 iot-biz 发送给 iot-gateway 的某个 "server"(protocol) 进行消费
     *
     * 其中，%s 就是该"server"(protocol) 的标识
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

    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 服务编号，该消息由哪个 server 发送
     */
    private String serverId;

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
    /**
     * 返回结果信息
     */
    private String msg;

    // ========== 基础方法：只传递"codec（编解码）字段" ==========

    public static IotDeviceMessage requestOf(String method) {
        return requestOf(null, method, null);
    }

    public static IotDeviceMessage requestOf(String method, Object params) {
        return requestOf(null, method, params);
    }

    public static IotDeviceMessage requestOf(String requestId, String method, Object params) {
        return of(requestId, method, params, null, null, null);
    }

    public static IotDeviceMessage replyOf(String requestId, String method,
                                           Object data, Integer code, String msg) {
        if (code == null) {
            code = GlobalErrorCodeConstants.SUCCESS.getCode();
            msg = GlobalErrorCodeConstants.SUCCESS.getMsg();
        }
        return of(requestId, method, null, data, code, msg);
    }

    public static IotDeviceMessage of(String requestId, String method,
                                      Object params, Object data, Integer code, String msg) {
        // 通用参数
        IotDeviceMessage message = new IotDeviceMessage()
                .setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now());
        // 当前参数
        message.setRequestId(requestId).setMethod(method).setParams(params)
                .setData(data).setCode(code).setMsg(msg);
        return message;
    }

    // ========== 核心方法：在 of 基础方法之上，添加对应 method ==========

    public static IotDeviceMessage buildStateUpdateOnline() {
        return requestOf(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(),
                MapUtil.of("state", IotDeviceStateEnum.ONLINE.getState()));
    }

    public static IotDeviceMessage buildStateOffline() {
        return requestOf(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(),
                MapUtil.of("state", IotDeviceStateEnum.OFFLINE.getState()));
    }

    public static IotDeviceMessage buildOtaUpgrade(String version, String fileUrl, Long fileSize,
                                                   String fileDigestAlgorithm, String fileDigestValue) {
        return requestOf(IotDeviceMessageMethodEnum.OTA_UPGRADE.getMethod(), MapUtil.builder()
                .put("version", version).put("fileUrl", fileUrl).put("fileSize", fileSize)
                .put("fileDigestAlgorithm", fileDigestAlgorithm).put("fileDigestValue", fileDigestValue)
                .build());
    }

}

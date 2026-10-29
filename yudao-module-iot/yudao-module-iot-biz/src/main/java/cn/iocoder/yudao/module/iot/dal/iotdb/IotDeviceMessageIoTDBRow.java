package cn.iocoder.yudao.module.iot.dal.iotdb;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IoTDB 设备消息表 device_message 的一行，字段为编码后的存储值
 *
 * 身份为 (deviceId, ts)；编解码见 {@link IotDeviceMessageIoTDBCodec}
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class IotDeviceMessageIoTDBRow {

    /**
     * 存储时间（毫秒），对应隐式 time 列
     */
    private Long ts;
    /**
     * 设备编号，唯一的 TAG
     */
    private String deviceId;
    /**
     * 消息编号，FIELD，原样存储
     */
    private String id;

    /**
     * 是否上行：-1 为空，0 为否，1 为是
     */
    private Integer upstream;
    /**
     * 是否回复：-1 为空，0 为否，1 为是
     */
    private Integer reply;

    // ========== 以下字段，统一使用 n:/v: 编码 ==========

    /**
     * 请求方法
     */
    private String method;
    /**
     * 标识符
     */
    private String identifier;
    /**
     * 请求编号
     */
    private String requestId;
    /**
     * 服务编号，该消息由哪个 server 发送
     */
    private String serverId;
    /**
     * 请求参数（JSON）
     */
    private String params;
    /**
     * 响应结果（JSON）
     */
    private String data;
    /**
     * 响应提示
     */
    private String msg;
    /**
     * 上报时间戳（毫秒）
     */
    private String reportTime;
    /**
     * 租户编号
     */
    private String tenantId;
    /**
     * 响应错误码
     */
    private String code;
    /**
     * 原始帧（Base64）
     */
    private String raw;

}

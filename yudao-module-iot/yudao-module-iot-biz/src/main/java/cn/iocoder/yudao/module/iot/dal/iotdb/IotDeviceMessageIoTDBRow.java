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

    private String method;
    private String identifier;
    private String requestId;
    private String serverId;
    private String params;
    private String data;
    private String msg;
    private String reportTime;
    private String tenantId;
    private String code;

}

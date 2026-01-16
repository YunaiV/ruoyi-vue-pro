package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * IoT Modbus 设备配置 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotModbusDeviceConfigRespDTO {

    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 租户编号
     */
    private Long tenantId;

    // ========== Modbus 连接配置 ==========

    /**
     * Modbus 服务器 IP 地址
     */
    private String ip;
    /**
     * Modbus 端口
     */
    private Integer port;
    /**
     * 从站地址
     */
    private Integer slaveId;
    /**
     * 连接超时时间（毫秒）
     */
    private Integer timeout;
    /**
     * 重试间隔（毫秒）
     */
    private Integer retryInterval;

    // ========== 点位配置 ==========

    /**
     * 点位列表
     */
    private List<IotModbusPointRespDTO> points;

}

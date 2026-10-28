package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT 设备信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceRespDTO {

    /**
     * 设备编号
     */
    private Long id;
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

    // ========== 产品相关字段 ==========

    /**
     * 产品编号
     */
    private Long productId;
    /**
     * 协议类型
     */
    private String protocolType;
    /**
     * 序列化类型
     */
    private String serializeType;
    /**
     * 是否启用数据解析脚本
     */
    private Boolean scriptEnabled;
    /**
     * 数据解析脚本（JS ES5）：只在启用时返回
     */
    private String scriptContent;

}
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
     * 编解码器类型
     */
    private String codecType;

}
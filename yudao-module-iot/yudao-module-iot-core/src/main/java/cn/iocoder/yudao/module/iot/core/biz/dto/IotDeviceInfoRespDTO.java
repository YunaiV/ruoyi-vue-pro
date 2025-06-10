package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT 设备信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceInfoRespDTO {

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
     * 设备密钥
     */
    private String deviceKey;

    /**
     * 租户编号
     */
    private Long tenantId;

}
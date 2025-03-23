package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import lombok.Data;

// TODO @芋艿：待实现：/ota/${productKey}/${deviceName}/progress
/**
 * IoT 设备【OTA】升级进度 Request DTO（上报更新固件进度）
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceOtaProgressReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    /**
     * 固件编号
     */
    private Long firmwareId;

    /**
     * 升级状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum}
     */
    private Integer status;
    /**
     * 升级进度，百分比
     */
    private Integer progress;

    /**
     * 升级进度描述
     */
    private String description;

}

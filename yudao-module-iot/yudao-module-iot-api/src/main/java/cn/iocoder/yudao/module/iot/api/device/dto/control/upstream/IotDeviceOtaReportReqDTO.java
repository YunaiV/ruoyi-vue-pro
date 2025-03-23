package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

// TODO @芋艿：待实现：/ota/${productKey}/${deviceName}/report
/**
 * IoT 设备【OTA】上报 Request DTO（上报固件版本）
 *
 * @author 芋道源码
 */
public class IotDeviceOtaReportReqDTO {

    /**
     * 固件编号
     */
    private Long firmwareId;

    /**
     * 固件版本
     */
    private String version;

}

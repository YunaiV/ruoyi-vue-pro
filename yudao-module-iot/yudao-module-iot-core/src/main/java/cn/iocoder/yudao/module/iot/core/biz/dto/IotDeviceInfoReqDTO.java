package cn.iocoder.yudao.module.iot.core.biz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * IoT 设备信息查询 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceInfoReqDTO {

    /**
     * 产品标识
     */
    @NotBlank(message = "产品标识不能为空")
    private String productKey;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

}
package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * IoT 设备下行的抽象 Request DTO
 *
 * @author 芋道源码
 */
@Data
public abstract class IotDeviceDownstreamAbstractReqDTO {

    /**
     * 请求编号
     */
    private String requestId;

    /**
     * 产品标识
     */
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;
    /**
     * 设备名称
     */
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

}

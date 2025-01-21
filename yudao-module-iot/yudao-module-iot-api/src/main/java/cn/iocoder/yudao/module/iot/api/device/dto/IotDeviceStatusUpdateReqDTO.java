package cn.iocoder.yudao.module.iot.api.device.dto;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备状态更新 Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotDeviceStatusUpdateReqDTO {

    // TODO 芋艿：要不要 id
    // TODO 芋艿：要不要 time

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
    /**
     * 设备状态
     */
    @NotEmpty(message = "设备状态不能为空")
    @InEnum(IotDeviceStatusEnum.class) // 只使用：在线、离线
    private Integer status;

}

package cn.iocoder.yudao.module.iot.api.device.dto;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * IoT 设备状态更新 Request DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class IotDeviceStatusUpdateReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    /**
     * 设备状态
     */
    @NotEmpty(message = "设备状态不能为空")
    @InEnum(IotDeviceStatusEnum.class) // 只使用：在线、离线
    private Integer status;

}

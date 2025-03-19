package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备状态更新 Request VO")
@Data
public class IotDeviceStatusUpdateReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long id;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备状态不能为空")
    @InEnum(IotDeviceStatusEnum.class)
    private Integer status;
}

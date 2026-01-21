package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - IoT 设备解绑网关 Request VO")
@Data
public class IotDeviceUnbindGatewayReqVO {

    @Schema(description = "子设备编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    @NotEmpty(message = "子设备编号列表不能为空")
    private Set<Long> ids;

}

package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 通过产品标识和设备名称列表获取设备 Request VO")
@Data
public class IotDeviceByProductKeyAndNamesReqVO {

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1de24640dfe")
    @NotBlank(message = "产品标识不能为空")
    private String productKey;

    @Schema(description = "设备名称列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "device001,device002")
    @NotEmpty(message = "设备名称列表不能为空")
    private List<String> deviceNames;

} 

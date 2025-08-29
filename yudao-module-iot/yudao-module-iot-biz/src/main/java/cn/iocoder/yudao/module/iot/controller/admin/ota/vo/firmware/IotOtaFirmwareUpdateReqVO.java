package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT OTA 固件更新 Request VO")
@Data
public class IotOtaFirmwareUpdateReqVO {

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "固件编号不能为空")
    private Long id;

    @Schema(description = "固件名称", example = "智能开关固件")
    private String name;

    @Schema(description = "固件描述", example = "某品牌型号固件，测试用")
    private String description;

}

package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT OTA 固件更新 Request VO")
@Data
public class IotOtaFirmwareUpdateReqVO {

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "固件编号不能为空")
    private Long id;

    // TODO @li：name 是不是可以飞必传哈
    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "智能开关固件")
    @NotEmpty(message = "固件名称不能为空")
    private String name;

    @Schema(description = "固件描述", example = "某品牌型号固件，测试用")
    private String description;

}

package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// TODO @li：因为 create 和 update 可以公用的字段比较少，建议不用 IotOtaFirmwareCommonReqVO
@Data
@Schema(description = "管理后台 - OTA固件信息 Request VO")
public class IotOtaFirmwareCommonReqVO {

    /**
     * 固件名称
     */
    @NotEmpty(message = "固件名称不能为空")
    @Schema(description = "固件名称", requiredMode = REQUIRED, example = "智能开关固件")
    private String name;

    /**
     * 固件描述
     */
    @Schema(description = "固件描述", example = "某品牌型号固件，测试用")
    private String description;

}

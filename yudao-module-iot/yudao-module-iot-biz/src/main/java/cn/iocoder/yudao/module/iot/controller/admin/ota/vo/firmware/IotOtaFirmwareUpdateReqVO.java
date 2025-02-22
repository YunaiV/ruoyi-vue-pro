package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@Schema(description = "管理后台 - OTA固件更新 Request VO")
public class IotOtaFirmwareUpdateReqVO extends IotOtaFirmwareCommonReqVO {

    /**
     * 固件编号
     */
    @NotNull(message = "固件编号不能为空")
    @Schema(description = "固件编号", requiredMode = REQUIRED, example = "1024")
    private Long id;

}

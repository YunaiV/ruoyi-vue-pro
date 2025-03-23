package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品脚本状态更新 Request VO")
@Data
public class IotProductScriptUpdateStatusReqVO {

    @Schema(description = "脚本ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "脚本ID不能为空")
    private Long id;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
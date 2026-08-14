package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - FMS 生成结转损益凭证 Request VO")
@Data
public class FmsProfitLossGenerateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotBlank(message = "会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "会计期间格式不正确")
    private String month;

}

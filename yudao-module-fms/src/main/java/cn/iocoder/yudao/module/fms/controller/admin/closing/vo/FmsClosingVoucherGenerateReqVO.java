package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - FMS 批量生成结转凭证 Request VO")
@Data
public class FmsClosingVoucherGenerateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "方案编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "方案编号不能为空")
    private List<@NotNull(message = "方案编号不能为空") Long> ids;

    @Schema(description = "会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotBlank(message = "会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "会计期间格式不正确")
    private String month;

}

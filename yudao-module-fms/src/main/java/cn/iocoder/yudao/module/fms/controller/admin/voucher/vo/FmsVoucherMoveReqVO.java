package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - FMS 凭证移动 Request VO")
@Data
public class FmsVoucherMoveReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "凭证月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotBlank(message = "凭证月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "凭证月份格式必须为 yyyy-MM")
    private String month;

    @Schema(description = "凭证字编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "凭证字不能为空")
    private Long voucherWordId;

    @Schema(description = "原凭证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    @NotNull(message = "原凭证号不能为空")
    @Min(value = 1, message = "原凭证号必须大于 0")
    private Integer sourceNumber;

    @Schema(description = "移动到的凭证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "移动到的凭证号不能为空")
    @Min(value = 1, message = "移动到的凭证号必须大于 0")
    private Integer targetNumber;

}

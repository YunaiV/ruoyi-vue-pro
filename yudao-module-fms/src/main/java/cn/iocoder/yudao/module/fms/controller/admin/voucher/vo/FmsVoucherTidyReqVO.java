package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherTidyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - FMS 凭证整理 Request VO")
@Data
public class FmsVoucherTidyReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "整理月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotBlank(message = "整理月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "整理月份格式必须为 yyyy-MM")
    private String month;

    @Schema(description = "凭证字编号，为空时整理全部凭证字", example = "11")
    private Long voucherWordId;

    @Schema(description = "起始编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "起始编号不能为空")
    @Min(value = 1, message = "起始编号必须大于 0")
    private Integer startNumber;

    @Schema(description = "整理方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "整理方式不能为空")
    @InEnum(FmsVoucherTidyTypeEnum.class)
    private Integer type;

}

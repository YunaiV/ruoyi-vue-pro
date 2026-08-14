package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - FMS 凭证汇总查询 Request VO")
@Data
public class FmsVoucherStatisticsReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "开始会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotNull(message = "开始会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "开始会计期间格式不正确")
    private String startMonth;

    @Schema(description = "结束会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-12")
    @NotNull(message = "结束会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结束会计期间格式不正确")
    private String endMonth;

    @Schema(description = "凭证字编号", example = "1024")
    private Long voucherWordId;

    @Schema(description = "最小凭证号", example = "1")
    @Min(value = 1, message = "最小凭证号不能小于 1")
    private Integer minVoucherNumber;

    @Schema(description = "最大凭证号", example = "100")
    @Min(value = 1, message = "最大凭证号不能小于 1")
    private Integer maxVoucherNumber;

    @Schema(description = "最小科目级次", example = "1")
    @Min(value = 1, message = "最小科目级次不能小于 1")
    @Max(value = 10, message = "最小科目级次不能大于 10")
    private Integer minLevel;

    @Schema(description = "最大科目级次", example = "1")
    @Min(value = 1, message = "最大科目级次不能小于 1")
    @Max(value = 10, message = "最大科目级次不能大于 10")
    private Integer maxLevel;

}

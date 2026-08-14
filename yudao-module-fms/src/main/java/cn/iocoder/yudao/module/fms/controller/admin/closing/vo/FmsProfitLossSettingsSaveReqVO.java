package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingVoucherTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - FMS 结转损益设置保存 Request VO")
@Data
public class FmsProfitLossSettingsSaveReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "凭证字编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "凭证字不能为空")
    private Long voucherWordId;

    @Schema(description = "凭证摘要", requiredMode = Schema.RequiredMode.REQUIRED, example = "结转损益")
    @NotBlank(message = "凭证摘要不能为空")
    @Size(max = 500, message = "凭证摘要长度不能超过 500 个字符")
    private String digest;

    @Schema(description = "结转凭证类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "结转凭证类型不能为空")
    @InEnum(FmsClosingVoucherTypeEnum.class)
    private Integer voucherType;

    @Schema(description = "以前年度损益调整科目编号", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1024")
    @NotNull(message = "以前年度损益调整科目不能为空")
    private Long priorYearAdjustmentSubjectId;

    @Schema(description = "以前年度损益调整结转科目编号", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1025")
    @NotNull(message = "以前年度损益调整结转科目不能为空")
    private Long adjustmentClosingSubjectId;

    @Schema(description = "其他损益结转科目编号", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1026")
    @NotNull(message = "其他损益结转科目不能为空")
    private Long otherClosingSubjectId;

    @Schema(description = "是否按余额反向结转", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结转方式不能为空")
    private Boolean reverseBalance;

    @Schema(description = "结转日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    @NotNull(message = "结转日期不能为空")
    @Min(value = 1, message = "结转日期不能小于 1")
    @Max(value = 31, message = "结转日期不能大于 31")
    private Integer closingDay;

}

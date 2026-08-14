package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 专用结转设置保存 Request VO")
@Data
public class FmsSpecialClosingSettingsSaveReqVO {

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "方案编号不能为空")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "凭证字编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "凭证字不能为空")
    private Long voucherWordId;

    @Schema(description = "结转科目规则数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "结转科目规则不能为空")
    @Size(min = 2, message = "结转科目规则至少需要两条")
    private List<SubjectRule> subjects;

    @Schema(description = "管理后台 - FMS 专用结转科目规则")
    @Data
    public static class SubjectRule {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "科目不能为空")
        private Long subjectId;

        @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED, example = "计提所得税")
        @NotBlank(message = "摘要不能为空")
        @Size(max = 500, message = "摘要长度不能超过 500 个字符")
        private String digest;

        @Schema(description = "借贷方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "借贷方向不能为空")
        @InEnum(FmsDebitCreditDirectionEnum.class)
        private Integer direction;

        @Schema(description = "金额比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
        @NotNull(message = "金额比例不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "金额比例必须大于 0")
        @DecimalMax(value = "100", message = "金额比例不能超过 100")
        private BigDecimal amountRatio;
    }

}

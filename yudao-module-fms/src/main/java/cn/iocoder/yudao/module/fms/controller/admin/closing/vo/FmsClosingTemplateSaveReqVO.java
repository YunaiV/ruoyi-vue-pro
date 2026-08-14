package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTemplateCategoryEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 结账模板保存 Request VO")
@Data
public class FmsClosingTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "报销差旅费")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 255, message = "模板名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "模板分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板分类不能为空")
    @InEnum(FmsClosingTemplateCategoryEnum.class)
    private Integer category;

    @Schema(description = "是否期末结转", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否期末结转不能为空")
    private Boolean periodEnd;

    @Schema(description = "来源科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "取数规则", example = "0")
    @InEnum(FmsFormulaRuleEnum.class)
    private Integer formulaRule;

    @Schema(description = "取数时间类型", example = "1")
    @InEnum(FmsClosingTimeTypeEnum.class)
    private Integer timeType;

    @Schema(description = "结转科目规则数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "结转科目规则不能为空")
    @Size(min = 2, message = "结转科目规则至少需要两条")
    private List<SubjectRule> subjects;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "管理后台 - FMS 结账模板科目规则")
    @Data
    public static class SubjectRule {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "科目不能为空")
        private Long subjectId;

        @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED, example = "报销差旅费")
        @NotBlank(message = "摘要不能为空")
        @Size(max = 500, message = "摘要长度不能超过 500 个字符")
        private String digest;

        @Schema(description = "借贷方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "借贷方向不能为空")
        @InEnum(FmsDebitCreditDirectionEnum.class)
        private Integer direction;

        @Schema(description = "金额比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "金额比例不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "金额比例必须大于 0")
        @DecimalMax(value = "100", message = "金额比例不能超过 100")
        private BigDecimal amountRatio;
    }

}

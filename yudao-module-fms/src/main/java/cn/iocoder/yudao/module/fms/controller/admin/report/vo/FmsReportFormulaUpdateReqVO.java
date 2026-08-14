package cn.iocoder.yudao.module.fms.controller.admin.report.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - FMS 报表公式更新 Request VO")
@Data
public class FmsReportFormulaUpdateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "配置编号不能为空")
    private Long id;

    @Schema(description = "公式项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "公式项数组不能为空")
    @Valid
    private List<Formula> formulas = new ArrayList<>();

    @Schema(description = "管理后台 - FMS 报表公式项 Request VO")
    @Data
    public static class Formula {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "科目编号不能为空")
        private Long subjectId;

        @Schema(description = "运算符", requiredMode = Schema.RequiredMode.REQUIRED, example = "+")
        @NotNull(message = "运算符不能为空")
        @Pattern(regexp = "[+-]", message = "运算符必须是 + 或 -")
        private String operator;

        @Schema(description = "取数规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        @NotNull(message = "取数规则不能为空")
        @InEnum(value = FmsFormulaRuleEnum.class, message = "取数规则必须是 {value}")
        private Integer rules;

    }

}

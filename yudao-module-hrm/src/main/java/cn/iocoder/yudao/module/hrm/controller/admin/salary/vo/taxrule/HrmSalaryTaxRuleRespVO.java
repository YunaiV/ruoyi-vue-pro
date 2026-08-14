package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 计税规则 Response VO")
@Data
public class HrmSalaryTaxRuleRespVO {

    @Schema(description = "计税规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "计税规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工资薪金所得税")
    private String name;

    @Schema(description = "计税类型", example = "1")
    private Integer type;

    @Schema(description = "是否计税", example = "true")
    private Boolean taxEnabled;

    @Schema(description = "起征阈值", example = "5000")
    private BigDecimal threshold;

    @Schema(description = "小数位数", example = "2")
    private Integer decimalScale;

    @Schema(description = "计税周期类型", example = "1")
    private Integer cycleType;

    @Schema(description = "使用该规则的薪资组数量", example = "2")
    private Long usedGroupCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

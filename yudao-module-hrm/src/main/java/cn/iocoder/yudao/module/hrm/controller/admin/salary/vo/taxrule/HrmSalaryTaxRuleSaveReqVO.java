package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;


@Schema(description = "管理后台 - HRM 计税规则保存 Request VO")
@Data
public class HrmSalaryTaxRuleSaveReqVO {

    @Schema(description = "计税规则编号", example = "1024")
    private Long id;

    @Schema(description = "计税规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工资薪金所得税")
    @NotBlank(message = "计税规则名称不能为空")
    @Size(max = 64, message = "计税规则名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "计税类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计税类型不能为空")
    @InEnum(value = HrmSalaryTaxTypeEnum.class, message = "计税类型必须是 {value}")
    private Integer type;

    @Schema(description = "是否计税", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否计税不能为空")
    private Boolean taxEnabled;

    @Schema(description = "起征阈值", example = "5000")
    @DecimalMin(value = "0.00", message = "起征阈值不能小于 0")
    @Digits(integer = 10, fraction = 2, message = "起征阈值最多 10 位整数和 2 位小数")
    private BigDecimal threshold;

    @Schema(description = "小数位数", example = "2")
    @Min(value = 0, message = "小数位数不能小于 0")
    @Max(value = 4, message = "小数位数不能大于 4")
    private Integer decimalScale;

    @Schema(description = "计税周期类型", example = "1")
    @InEnum(value = HrmSalaryTaxCycleTypeEnum.class, message = "计税周期类型必须是 {value}")
    private Integer cycleType;

    @AssertTrue(message = "计税规则的起征阈值和小数位数不能为空")
    @JsonIgnore
    public boolean isTaxConfigValid() {
        return HrmSalaryTaxTypeEnum.NONE.getType().equals(type) || threshold != null && decimalScale != null;
    }

    @AssertTrue(message = "工资薪金所得税的计税周期不能为空")
    @JsonIgnore
    public boolean isCycleTypeValid() {
        return ObjUtil.notEqual(HrmSalaryTaxTypeEnum.SALARY.getType(), type) || cycleType != null;
    }

}

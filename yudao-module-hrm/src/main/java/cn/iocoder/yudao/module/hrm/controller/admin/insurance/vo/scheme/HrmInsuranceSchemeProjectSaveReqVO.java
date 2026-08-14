package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 社保方案项目保存 Request VO")
@Data
public class HrmInsuranceSchemeProjectSaveReqVO {

    @Schema(description = "社保方案项目编号", example = "1024")
    private Long id;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目类型不能为空")
    @InEnum(value = HrmInsuranceProjectTypeEnum.class, message = "项目类型必须是 {value}")
    private Integer type;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "养老保险")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 64, message = "项目名称不能超过 64 个字符")
    private String name;

    @Schema(description = "缴纳基数", example = "10000.00")
    @DecimalMin(value = "0.00", message = "缴纳基数不能小于 0")
    @Digits(integer = 10, fraction = 2, message = "缴纳基数最多 10 位整数和 2 位小数")
    private BigDecimal baseAmount;

    @Schema(description = "公司缴纳比例", example = "16.00")
    @DecimalMin(value = "0.00", message = "公司缴纳比例不能小于 0")
    @DecimalMax(value = "100.00", message = "公司缴纳比例不能大于 100")
    @Digits(integer = 3, fraction = 2, message = "公司缴纳比例最多 2 位小数")
    private BigDecimal corporateRate;

    @Schema(description = "个人缴纳比例", example = "8.00")
    @DecimalMin(value = "0.00", message = "个人缴纳比例不能小于 0")
    @DecimalMax(value = "100.00", message = "个人缴纳比例不能大于 100")
    @Digits(integer = 3, fraction = 2, message = "个人缴纳比例最多 2 位小数")
    private BigDecimal personalRate;

    @Schema(description = "公司缴纳金额", example = "1600.00")
    @DecimalMin(value = "0.00", message = "公司缴纳金额不能小于 0")
    @Digits(integer = 10, fraction = 2, message = "公司缴纳金额最多 10 位整数和 2 位小数")
    private BigDecimal corporateAmount;

    @Schema(description = "个人缴纳金额", example = "800.00")
    @DecimalMin(value = "0.00", message = "个人缴纳金额不能小于 0")
    @Digits(integer = 10, fraction = 2, message = "个人缴纳金额最多 10 位整数和 2 位小数")
    private BigDecimal personalAmount;

}

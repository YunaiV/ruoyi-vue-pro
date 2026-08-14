package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 标准参保项目 Response VO")
@Data
public class HrmInsuranceStandardProjectRespVO {

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "养老保险")
    private String name;

    @Schema(description = "缴纳基数", example = "10000.00")
    private BigDecimal baseAmount;

    @Schema(description = "公司缴纳比例", example = "16.00")
    private BigDecimal corporateRate;

    @Schema(description = "个人缴纳比例", example = "8.00")
    private BigDecimal personalRate;

    @Schema(description = "公司缴纳金额", example = "1600.00")
    private BigDecimal corporateAmount;

    @Schema(description = "个人缴纳金额", example = "800.00")
    private BigDecimal personalAmount;

}

package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 社保方案项目 Response VO")
@Data
public class HrmInsuranceSchemeProjectRespVO {

    @Schema(description = "社保方案项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "社保方案编号", example = "1024")
    private Long schemeId;

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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

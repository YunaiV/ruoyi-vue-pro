package cn.iocoder.yudao.module.hrm.controller.admin.portal.insurance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工端社保项目 Response VO")
@Data
public class HrmPortalInsuranceProjectRespVO {

    @Schema(description = "社保项目编号")
    private Long schemeProjectId;

    @Schema(description = "员工端社保项目类型")
    private Integer type;

    @Schema(description = "社保项目名称")
    private String name;

    @Schema(description = "默认缴纳金额")
    private BigDecimal baseAmount;

    @Schema(description = "公司缴纳比例")
    private BigDecimal corporateRate;

    @Schema(description = "个人缴纳比例")
    private BigDecimal personalRate;

    @Schema(description = "公司缴纳金额")
    private BigDecimal corporateAmount;

    @Schema(description = "个人缴纳金额")
    private BigDecimal personalAmount;

}

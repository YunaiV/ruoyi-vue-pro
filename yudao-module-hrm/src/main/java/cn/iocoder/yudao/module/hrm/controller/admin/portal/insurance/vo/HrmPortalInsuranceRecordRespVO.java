package cn.iocoder.yudao.module.hrm.controller.admin.portal.insurance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工端社保记录 Response VO")
@Data
public class HrmPortalInsuranceRecordRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "月度社保记录编号")
    private Long monthRecordId;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "社保方案编号")
    private Long schemeId;

    @Schema(description = "社保方案名称")
    private String schemeName;

    @Schema(description = "社保方案类型")
    private Integer schemeType;

    @Schema(description = "参保城市")
    private String schemeCity;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "个人社保金额")
    private BigDecimal personalInsuranceAmount;

    @Schema(description = "个人公积金金额")
    private BigDecimal personalProvidentFundAmount;

    @Schema(description = "公司社保金额")
    private BigDecimal corporateInsuranceAmount;

    @Schema(description = "公司公积金金额")
    private BigDecimal corporateProvidentFundAmount;

    @Schema(description = "员工端社保记录状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "社保项目列表")
    private List<HrmPortalInsuranceProjectRespVO> projects;

}

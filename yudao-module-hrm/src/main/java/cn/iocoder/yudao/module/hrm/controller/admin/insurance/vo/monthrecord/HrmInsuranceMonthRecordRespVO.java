package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 月度社保表 Response VO")
@Data
public class HrmInsuranceMonthRecordRespVO {

    @Schema(description = "月度社保表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer month;

    @Schema(description = "标题", example = "2026 年 7 月社保表")
    private String title;

    @Schema(description = "参保人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer insuredEmployeeCount;

    @Schema(description = "停保人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer stoppedEmployeeCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "个人社保金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "800.00")
    private BigDecimal personalInsuranceAmount;

    @Schema(description = "个人公积金金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "700.00")
    private BigDecimal personalProvidentFundAmount;

    @Schema(description = "企业社保金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1600.00")
    private BigDecimal corporateInsuranceAmount;

    @Schema(description = "企业公积金金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "700.00")
    private BigDecimal corporateProvidentFundAmount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

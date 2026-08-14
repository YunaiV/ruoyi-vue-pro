package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionNodeRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 月度工资表 Response VO")
@Data
public class HrmSalaryMonthRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer month;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026 年 7 月工资表")
    private String title;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "员工数量")
    private Integer employeeCount;

    @Schema(description = "应发工资")
    private BigDecimal expectedPaySalary;

    @Schema(description = "个人社保金额")
    private BigDecimal personalInsuranceAmount;

    @Schema(description = "个人公积金金额")
    private BigDecimal personalProvidentFundAmount;

    @Schema(description = "个人所得税")
    private BigDecimal personalTax;

    @Schema(description = "实发工资")
    private BigDecimal realPaySalary;

    @Schema(description = "公司社保金额")
    private BigDecimal corporateInsuranceAmount;

    @Schema(description = "公司公积金金额")
    private BigDecimal corporateProvidentFundAmount;

    @Schema(description = "核算状态")
    private Integer status;

    @Schema(description = "薪资项表头")
    private List<HrmSalaryOptionNodeRespVO> optionHeaders;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

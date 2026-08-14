package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 计薪配置 Response VO")
@Data
public class HrmSalaryConfigRespVO {

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "计薪周期开始日", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer cycleStartDay;

    @Schema(description = "计薪周期结束日", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    private Integer cycleEndDay;

    @Schema(description = "社保对应月份类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer socialSecurityMonthType;

    @Schema(description = "工资开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer startYear;

    @Schema(description = "工资开始月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer startMonth;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 员工业绩统计 Request VO")
@Data
public class CrmStatisticsStaffPerformanceReqVO {

    @Schema(description = "部门 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门 id 不能为空")
    private Long deptId;


    @Schema(description = "年", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR)
    @NotEmpty(message = "时间不能为空")
    private LocalDateTime year;

}

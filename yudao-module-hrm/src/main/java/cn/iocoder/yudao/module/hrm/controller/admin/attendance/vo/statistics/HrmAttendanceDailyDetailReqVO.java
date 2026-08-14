package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 员工每日考勤明细 Request VO")
@Data
public class HrmAttendanceDailyDetailReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @Schema(description = "考勤日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "考勤日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime attendanceTime;

}

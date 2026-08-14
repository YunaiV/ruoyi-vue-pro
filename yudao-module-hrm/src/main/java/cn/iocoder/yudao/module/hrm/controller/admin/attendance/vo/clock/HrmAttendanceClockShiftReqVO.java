package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 员工实际班次 Request VO")
@Data
public class HrmAttendanceClockShiftReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @Schema(description = "考勤时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "考勤时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime attendanceTime;

}

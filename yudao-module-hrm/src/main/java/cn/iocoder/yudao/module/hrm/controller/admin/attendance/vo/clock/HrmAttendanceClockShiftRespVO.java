package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工实际班次 Response VO")
@Data
public class HrmAttendanceClockShiftRespVO {

    @Schema(description = "上班时间")
    private LocalDateTime startTime;

    @Schema(description = "下班时间")
    private LocalDateTime endTime;

    @Schema(description = "上班打卡开始时间")
    private LocalDateTime clockInStartTime;

    @Schema(description = "上班打卡结束时间")
    private LocalDateTime clockInEndTime;

    @Schema(description = "下班打卡开始时间")
    private LocalDateTime clockOutStartTime;

    @Schema(description = "下班打卡结束时间")
    private LocalDateTime clockOutEndTime;

}

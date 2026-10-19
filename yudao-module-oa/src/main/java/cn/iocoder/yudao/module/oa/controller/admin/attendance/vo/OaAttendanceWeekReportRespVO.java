package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 考勤周报 Response VO")
@Data
@Accessors(chain = true)
public class OaAttendanceWeekReportRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道")
    private String userName;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "每日考勤列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DailyAttendance> dailyAttendances;

    @Schema(description = "管理后台 - OA 每日考勤 Response VO")
    @Data
    @Accessors(chain = true)
    public static class DailyAttendance {

        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-07")
        private LocalDate date;

        @Schema(description = "上班考勤记录编号", example = "1024")
        private Long clockInId;

        @Schema(description = "上班打卡时间", type = "integer", format = "int64", example = "1789347600000")
        private LocalDateTime clockInTime;

        @Schema(description = "上班打卡状态", example = "1")
        private Integer clockInStatus;

        @Schema(description = "下班考勤记录编号", example = "1025")
        private Long clockOutId;

        @Schema(description = "下班打卡时间", type = "integer", format = "int64", example = "1789380000000")
        private LocalDateTime clockOutTime;

        @Schema(description = "下班打卡状态", example = "1")
        private Integer clockOutStatus;

    }

}

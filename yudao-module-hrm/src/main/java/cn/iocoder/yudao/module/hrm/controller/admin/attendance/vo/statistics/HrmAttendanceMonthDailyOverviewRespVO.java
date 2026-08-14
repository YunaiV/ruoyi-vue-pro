package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Schema(description = "管理后台 - HRM 员工月度打卡概况 Response VO")
@Data
public class HrmAttendanceMonthDailyOverviewRespVO {

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "每日打卡概况，Key 为日期")
    private Map<LocalDate, HrmAttendanceDailyOverviewRespVO> dailyClockMap;

}

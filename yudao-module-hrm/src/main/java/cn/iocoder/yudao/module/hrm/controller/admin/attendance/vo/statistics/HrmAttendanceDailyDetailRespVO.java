package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工每日考勤明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmAttendanceDailyDetailRespVO {

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

    @ExcelProperty(value = "日期", index = 0)
    @Schema(description = "日期")
    private LocalDateTime attendanceTime;

    @ExcelProperty(value = "班次", index = 1)
    @Schema(description = "班次")
    private String shiftName;

    @Schema(description = "是否排班")
    private Boolean scheduled;

    @Schema(description = "应打卡次数")
    private Integer requiredClockCount;

    @Schema(description = "排班分钟数")
    private Integer scheduledMinutes;

    @ExcelProperty(value = "缺卡次数", index = 8)
    @Schema(description = "缺卡次数")
    private Integer misscardCount;

    @Schema(description = "是否旷工")
    private Boolean absenteeism;

    @Schema(description = "旷工分钟数")
    private Integer absenteeismMinutes;

    @ExcelProperty(value = "旷工天数", index = 9)
    @Schema(description = "旷工天数")
    private BigDecimal absenteeismDays;

    @Schema(description = "是否请假")
    private Boolean leaveStatus;

    @Schema(description = "请假分钟数")
    private Integer leaveMinutes;

    @ExcelProperty(value = "请假天数", index = 10)
    @Schema(description = "请假天数")
    private BigDecimal leaveDays;

    @ExcelProperty(value = "考勤结果", index = 2)
    @Schema(description = "考勤结果")
    private String attendanceResult;

    @ExcelProperty(value = "打卡次数", index = 3)
    @Schema(description = "打卡次数")
    private Integer clockCount;

    @ExcelProperty(value = "迟到次数", index = 4)
    @Schema(description = "迟到次数")
    private Integer lateCount;

    @ExcelProperty(value = "迟到分钟", index = 5)
    @Schema(description = "迟到分钟")
    private Integer lateMinutes;

    @ExcelProperty(value = "早退次数", index = 6)
    @Schema(description = "早退次数")
    private Integer earlyCount;

    @ExcelProperty(value = "早退分钟", index = 7)
    @Schema(description = "早退分钟")
    private Integer earlyMinutes;

    @Schema(description = "打卡列表")
    private List<HrmAttendanceClockRespVO> clockList;

}

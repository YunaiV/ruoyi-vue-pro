package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工月度考勤汇总 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmAttendanceMonthRecordRespVO {

    @ExcelProperty(value = "员工编号", index = 0)
    @Schema(description = "员工编号")
    private Long employeeId;

    @ExcelProperty(value = "员工姓名", index = 1)
    @Schema(description = "员工姓名")
    private String employeeName;

    @ExcelProperty(value = "工号", index = 2)
    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "部门编号")
    private Long deptId;

    @ExcelProperty(value = "部门", index = 3)
    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位名称")
    @ExcelProperty(value = "岗位", index = 4)
    private String postName;

    @Schema(description = "考勤组名称")
    private String attendanceGroupName;

    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "员工状态")
    private Integer employeeStatus;

    @Schema(description = "工作城市")
    private String workCity;

    @ExcelProperty(value = "年份", index = 5)
    @Schema(description = "年份")
    private Integer year;

    @ExcelProperty(value = "月份", index = 6)
    @Schema(description = "月份")
    private Integer month;

    @ExcelProperty(value = "应出勤天数", index = 7)
    @Schema(description = "应出勤天数")
    private Integer attendDays;

    @ExcelProperty(value = "实际出勤天数", index = 8)
    @Schema(description = "实际出勤天数")
    private BigDecimal actualDays;

    @ExcelProperty(value = "迟到时长（分钟）", index = 9)
    @Schema(description = "迟到时长（分钟）")
    private Integer lateMinute;

    @ExcelProperty(value = "迟到次数", index = 10)
    @Schema(description = "迟到次数")
    private Integer lateCount;

    @ExcelProperty(value = "早退时长（分钟）", index = 11)
    @Schema(description = "早退时长（分钟）")
    private Integer earlyMinute;

    @ExcelProperty(value = "早退次数", index = 12)
    @Schema(description = "早退次数")
    private Integer earlyCount;

    @ExcelProperty(value = "缺卡次数", index = 13)
    @Schema(description = "缺卡次数")
    private Integer misscardCount;

    @ExcelProperty(value = "旷工天数", index = 14)
    @Schema(description = "旷工天数")
    private BigDecimal absenteeismDays;

    @ExcelProperty(value = "旷工时长（分钟）", index = 15)
    @Schema(description = "旷工时长（分钟）")
    private Integer absenteeismMinutes;

    @ExcelProperty(value = "请假天数", index = 16)
    @Schema(description = "请假天数")
    private BigDecimal leaveDays;

    @ExcelProperty(value = "请假时长（分钟）", index = 17)
    @Schema(description = "请假时长（分钟）")
    private Integer leaveMinutes;

    @ExcelProperty(value = "迟到扣款", index = 18)
    @Schema(description = "迟到扣款")
    private BigDecimal lateDeductAmount;

    @ExcelProperty(value = "早退扣款", index = 19)
    @Schema(description = "早退扣款")
    private BigDecimal earlyDeductAmount;

    @ExcelProperty(value = "缺卡扣款", index = 20)
    @Schema(description = "缺卡扣款")
    private BigDecimal misscardDeductAmount;

    @ExcelProperty(value = "旷工扣款", index = 21)
    @Schema(description = "旷工扣款")
    private BigDecimal absenteeismDeductAmount;

    @ExcelProperty(value = "考勤扣款合计", index = 22)
    @Schema(description = "考勤扣款合计")
    private BigDecimal attendanceDeductAmount;

    @Schema(description = "是否全勤")
    private Boolean fullAttendance;

}

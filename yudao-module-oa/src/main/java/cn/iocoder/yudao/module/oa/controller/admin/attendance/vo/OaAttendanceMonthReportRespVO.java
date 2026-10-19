package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - OA 考勤月报 Response VO")
@Data
@Accessors(chain = true)
public class OaAttendanceMonthReportRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道")
    private String userName;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "上班打卡次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer clockInCount;

    @Schema(description = "下班打卡次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "19")
    private Integer clockOutCount;

    @Schema(description = "正常次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "36")
    private Integer normalCount;

    @Schema(description = "迟到次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer lateCount;

    @Schema(description = "早退次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer earlyCount;

    @Schema(description = "请假天数，审批通过且开始时间在当月的申请天数合计", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer leaveDays;

    @Schema(description = "出差天数，审批通过且开始时间在当月的申请天数合计", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer travelDays;

    @Schema(description = "旷工天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer absentDays;

}

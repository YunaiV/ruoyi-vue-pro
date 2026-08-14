package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 请假记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmAttendanceLeaveRespVO {

    @Schema(description = "请假记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty(value = "记录编号", index = 0)
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @ExcelProperty(value = "员工编号", index = 1)
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    @ExcelProperty(value = "员工姓名", index = 2)
    private String employeeName;

    @Schema(description = "工号", example = "HRM001")
    @ExcelProperty(value = "工号", index = 3)
    private String jobNumber;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    @ExcelProperty(value = "部门", index = 4)
    private String deptName;

    @Schema(description = "职位名称", example = "Java 工程师")
    @ExcelProperty(value = "岗位", index = 5)
    private String postName;

    @Schema(description = "请假类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "事假")
    @ExcelProperty(value = "请假类型", index = 6, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_ATTENDANCE_LEAVE_TYPE)
    private String type;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "开始时间", index = 7)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "结束时间", index = 8)
    private LocalDateTime endTime;

    @Schema(description = "请假天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    @ExcelProperty(value = "请假天数", index = 9)
    private BigDecimal day;

    @Schema(description = "请假事由", example = "个人事务")
    @ExcelProperty(value = "请假事由", index = 10)
    private String reason;

    @Schema(description = "备注")
    @ExcelProperty(value = "备注", index = 11)
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "创建时间", index = 12)
    private LocalDateTime createTime;

    @Schema(description = "审批状态", example = "1")
    private Integer approvalStatus;

    @Schema(description = "流程实例编号")
    private String processInstanceId;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批意见")
    private String approvalReason;

}

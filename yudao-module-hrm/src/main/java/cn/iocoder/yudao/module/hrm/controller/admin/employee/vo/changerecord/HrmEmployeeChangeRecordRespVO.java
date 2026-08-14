package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工异动记录 Response VO")
@Data
public class HrmEmployeeChangeRecordRespVO {

    @Schema(description = "异动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long employeeId;

    @Schema(description = "变动类型", example = "5")
    private Integer type;

    @Schema(description = "异动原因", example = "1")
    private Integer reason;

    @Schema(description = "原部门编号", example = "100")
    private Long oldDeptId;

    @Schema(description = "原部门名称", example = "研发部")
    private String oldDeptName;

    @Schema(description = "新部门编号", example = "200")
    private Long newDeptId;

    @Schema(description = "新部门名称", example = "产品部")
    private String newDeptName;

    @Schema(description = "原岗位名称")
    private String oldPostName;

    @Schema(description = "新岗位名称")
    private String newPostName;

    @Schema(description = "原职级")
    private String oldPostLevel;

    @Schema(description = "新职级")
    private String newPostLevel;

    @Schema(description = "原工作地点")
    private String oldWorkAddress;

    @Schema(description = "新工作地点")
    private String newWorkAddress;

    @Schema(description = "原直属上级员工编号")
    private Long oldLeaderEmployeeId;

    @Schema(description = "原直属上级员工姓名")
    private String oldLeaderEmployeeName;

    @Schema(description = "新直属上级员工编号")
    private Long newLeaderEmployeeId;

    @Schema(description = "新直属上级员工姓名")
    private String newLeaderEmployeeName;

    @Schema(description = "试用期，单位月")
    private Integer probation;

    @Schema(description = "生效日期")
    private LocalDateTime effectTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

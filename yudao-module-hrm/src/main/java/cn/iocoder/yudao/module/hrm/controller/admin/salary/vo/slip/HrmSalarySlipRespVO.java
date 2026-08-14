package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 工资条 Response VO")
@Data
public class HrmSalarySlipRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "工资条记录编号")
    private Long sendRecordId;

    @Schema(description = "员工工资记录编号")
    private Long monthEmployeeRecordId;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "已读状态")
    private Integer readStatus;

    @Schema(description = "实际工资")
    private BigDecimal realPaySalary;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "选项列表")
    private List<HrmSalarySlipOptionRespVO> options;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

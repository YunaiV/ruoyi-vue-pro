package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 工资条待发员工 Response VO")
@Data
public class HrmSalarySlipSendEmployeeRespVO {

    @Schema(description = "员工月度工资记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long monthEmployeeRecordId;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "工号", example = "HRM001")
    private String jobNumber;

    @Schema(description = "手机号码", example = "13800138000")
    private String mobile;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "岗位名称", example = "Java 工程师")
    private String postName;

    @Schema(description = "应发工资", example = "10000")
    private BigDecimal expectedPaySalary;

    @Schema(description = "实发工资", example = "8500")
    private BigDecimal realPaySalary;

    @Schema(description = "是否已发送", example = "false")
    private Boolean sent;

}

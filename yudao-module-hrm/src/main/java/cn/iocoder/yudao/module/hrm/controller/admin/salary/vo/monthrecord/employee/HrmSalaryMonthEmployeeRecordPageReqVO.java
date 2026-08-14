package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeChangeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工工资表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryMonthEmployeeRecordPageReqVO extends PageParam {

    @Schema(description = "工资表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工资表编号不能为空")
    private Long monthRecordId;

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "工号", example = "HRM001")
    private String jobNumber;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "员工异动类型", example = "1")
    @InEnum(value = HrmSalaryEmployeeChangeTypeEnum.class, message = "员工异动分类必须是 {value}")
    private Integer employeeChangeType;

    @Schema(description = "员工编号列表", example = "[1024, 1025]")
    private List<Long> employeeIds;

    @Schema(hidden = true)
    private Boolean salarySlipSent;
}

package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工月度工资 Response VO")
@Data
public class HrmSalaryMonthEmployeeRecordRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "工资表编号")
    private Long monthRecordId;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门")
    private String deptName;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "计薪出勤天数")
    private BigDecimal actualWorkDay;

    @Schema(description = "应出勤天数")
    private BigDecimal needWorkDay;

    @Schema(description = "应发工资")
    private BigDecimal expectedPaySalary;

    @Schema(description = "应税工资")
    private BigDecimal taxableSalary;

    @Schema(description = "个人所得税")
    private BigDecimal personalTax;

    @Schema(description = "实发工资")
    private BigDecimal realPaySalary;

    @Schema(description = "绩效系数")
    private BigDecimal performanceCoefficient;

    @Schema(description = "薪资项值列表")
    private List<HrmSalaryOptionValueVO> optionValues;

}

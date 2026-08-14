package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工薪资信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmSalaryEmployeeInfoRespVO {

    @ExcelProperty(value = "员工薪资编号", index = 0)
    @Schema(description = "员工薪资编号")
    private Long id;

    @ExcelProperty(value = "员工编号", index = 1)
    @Schema(description = "员工编号")
    private Long employeeId;

    @ExcelProperty(value = "员工姓名", index = 2)
    @Schema(description = "员工姓名")
    private String employeeName;

    @ExcelProperty(value = "工号", index = 3)
    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "手机号")
    private String mobile;

    @ExcelProperty(value = "部门编号", index = 4)
    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "入职状态")
    private Integer entryStatus;

    @Schema(description = "员工状态")
    private Integer status;

    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "转正时间")
    private LocalDateTime regularTime;

    @ExcelProperty(value = "调整原因", index = 6, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_SALARY_CHANGE_REASON)
    @Schema(description = "调整原因")
    private Integer changeReason;

    @ExcelProperty(value = "生效日期", index = 7)
    @Schema(description = "生效日期")
    private LocalDateTime effectTime;

    @ExcelProperty(value = "薪资状态", index = 5, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_SALARY_CHANGE_TYPE)
    @Schema(description = "薪资状态")
    private Integer changeType;

    @ExcelProperty(value = "试用期工资", index = 9)
    @Schema(description = "试用期工资")
    private BigDecimal probationSalary;

    @ExcelProperty(value = "正式工资", index = 8)
    @Schema(description = "正式工资")
    private BigDecimal regularSalary;

    @ExcelProperty(value = "备注", index = 10)
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "薪资项列表")
    private List<HrmSalaryOptionValueVO> salaryOptions;

    @Schema(description = "试用期薪资项列表")
    private List<HrmSalaryOptionValueVO> probationSalaryOptions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

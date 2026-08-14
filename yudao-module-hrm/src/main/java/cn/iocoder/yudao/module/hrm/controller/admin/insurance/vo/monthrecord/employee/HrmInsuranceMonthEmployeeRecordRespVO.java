package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工月度社保 Response VO")
@Data
public class HrmInsuranceMonthEmployeeRecordRespVO {

    @Schema(description = "记录编号")
    private Long id;

    @Schema(description = "社保表编号")
    private Long monthRecordId;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门")
    private String deptName;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "入职状态")
    private Integer entryStatus;

    @Schema(description = "员工状态")
    private Integer employeeStatus;

    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "社保方案编号")
    private Long schemeId;

    @Schema(description = "社保方案")
    private String schemeName;

    @Schema(description = "参保地区")
    private String areaName;

    @Schema(description = "参保地区编号")
    private Integer areaId;

    @Schema(description = "户籍类型")
    private String houseType;

    @Schema(description = "社保方案类型")
    private Integer schemeType;

    @Schema(description = "个人社保号")
    private String socialSecurityNumber;

    @Schema(description = "个人公积金号")
    private String accumulationFundNumber;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "个人社保")
    private BigDecimal personalInsuranceAmount;

    @Schema(description = "个人公积金")
    private BigDecimal personalProvidentFundAmount;

    @Schema(description = "公司社保")
    private BigDecimal corporateInsuranceAmount;

    @Schema(description = "公司公积金")
    private BigDecimal corporateProvidentFundAmount;

    @Schema(description = "参保状态")
    private Integer status;

    @Schema(description = "社保项目列表")
    private List<HrmInsuranceMonthEmployeeProjectRespVO> socialSecurityProjectList;

    @Schema(description = "公积金项目列表")
    private List<HrmInsuranceMonthEmployeeProjectRespVO> providentFundProjectList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

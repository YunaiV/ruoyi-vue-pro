package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.AreaConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.framework.excel.core.HrmEmployeeProbationExcelColumnSelectFunction;
import cn.iocoder.yudao.module.hrm.framework.excel.core.HrmRecruitChannelExcelColumnSelectFunction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工档案 Excel 导入 VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class HrmEmployeeImportExcelVO {

    @ExcelProperty("员工姓名")
    @Schema(description = "员工姓名")
    private String name;

    @ExcelProperty("工号")
    @Schema(description = "工号")
    private String jobNumber;

    @ExcelProperty("手机号")
    @Schema(description = "手机号")
    private String mobile;

    @ExcelProperty("国家或地区")
    @Schema(description = "国家或地区")
    private String country;

    @ExcelProperty("民族")
    @Schema(description = "民族")
    private String nation;

    @ExcelProperty(value = "证件类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_ID_TYPE)
    @ExcelColumnSelect(dictType = DictTypeConstants.HRM_EMPLOYEE_ID_TYPE)
    @Schema(description = "证件类型")
    private Integer idType;

    @ExcelProperty("证件号码")
    @Schema(description = "证件号码")
    private String idNumber;

    @ExcelProperty(value = "性别", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.system.enums.DictTypeConstants.USER_SEX)
    @ExcelColumnSelect(dictType = cn.iocoder.yudao.module.system.enums.DictTypeConstants.USER_SEX)
    @Schema(description = "性别")
    private Integer sex;

    @ExcelProperty("邮箱")
    @Schema(description = "邮箱")
    private String email;

    @ExcelProperty("籍贯")
    @Schema(description = "籍贯")
    private String nativePlace;

    @ExcelProperty("出生日期")
    @Schema(description = "出生日期")
    private LocalDateTime birthday;

    @ExcelProperty("户籍地址")
    @Schema(description = "户籍地址")
    private String address;

    @ExcelProperty(value = "最高学历", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_EDUCATION)
    @ExcelColumnSelect(dictType = DictTypeConstants.HRM_EMPLOYEE_EDUCATION)
    @Schema(description = "最高学历")
    private Integer highestEducation;

    @ExcelProperty("部门编号")
    @Schema(description = "部门编号")
    private Long deptId;

    @ExcelProperty("直属上级工号")
    @Schema(description = "直属上级工号")
    private String leaderJobNumber;

    @ExcelProperty("职位名称")
    @Schema(description = "职位名称")
    private String postName;

    @ExcelProperty("岗位职级")
    @Schema(description = "岗位职级")
    private String postLevel;

    @ExcelProperty(value = "入职状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_ENTRY_STATUS)
    @ExcelColumnSelect(dictType = DictTypeConstants.HRM_EMPLOYEE_ENTRY_STATUS)
    @Schema(description = "入职状态")
    private Integer entryStatus;

    @ExcelProperty(value = "员工状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_STATUS)
    @ExcelColumnSelect(dictType = DictTypeConstants.HRM_EMPLOYEE_STATUS)
    @Schema(description = "员工状态")
    private Integer status;

    @ExcelProperty(value = "聘用形式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_TYPE)
    @ExcelColumnSelect(dictType = DictTypeConstants.HRM_EMPLOYEE_TYPE)
    @Schema(description = "聘用形式")
    private Integer type;

    @ExcelProperty("入职时间")
    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @ExcelProperty("试用期（月）")
    @ExcelColumnSelect(functionName = HrmEmployeeProbationExcelColumnSelectFunction.NAME)
    @Schema(description = "试用期（月）")
    private Integer probation;

    @ExcelProperty("转正时间")
    @Schema(description = "转正时间")
    private LocalDateTime regularTime;

    @ExcelProperty("离职时间")
    @Schema(description = "离职时间")
    private LocalDateTime leaveTime;

    @ExcelProperty("工作城市")
    @Schema(description = "工作城市")
    private String workCity;

    @ExcelProperty("工作地点")
    @Schema(description = "工作地点")
    private String workAddress;

    @ExcelProperty("工作详细地址")
    @Schema(description = "工作详细地址")
    private String workDetailAddress;

    @ExcelProperty("招聘渠道")
    @ExcelColumnSelect(functionName = HrmRecruitChannelExcelColumnSelectFunction.NAME)
    @Schema(description = "招聘渠道选项，格式为名称（ID）")
    private String channelName;

    @ExcelProperty("司龄起算时间")
    @Schema(description = "司龄起算时间")
    private LocalDateTime companyAgeStartTime;

    @ExcelProperty("绑定用户手机号")
    @Schema(description = "绑定后台用户的手机号")
    private String userMobile;

    @ExcelProperty("银行卡号")
    @Schema(description = "银行卡号")
    private String bankCardNumber;

    @ExcelProperty(value = "开户地区", converter = AreaConvert.class)
    @Schema(description = "开户地区")
    private Integer bankAreaId;

    @ExcelProperty("银行名称")
    @Schema(description = "银行名称")
    private String bankName;

    @ExcelProperty("开户支行名称")
    @Schema(description = "开户支行名称")
    private String bankBranchName;

    @ExcelProperty(value = "是否首次参保", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    @ExcelColumnSelect(dictType = DictTypeConstants.BOOLEAN_STRING)
    @Schema(description = "是否首次参保")
    private Boolean firstSocialSecurity;

    @ExcelProperty(value = "是否首次缴纳公积金", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    @ExcelColumnSelect(dictType = DictTypeConstants.BOOLEAN_STRING)
    @Schema(description = "是否首次缴纳公积金")
    private Boolean firstAccumulationFund;

    @ExcelProperty("个人社保账号")
    @Schema(description = "个人社保账号")
    private String socialSecurityNumber;

    @ExcelProperty("个人公积金账号")
    @Schema(description = "个人公积金账号")
    private String accumulationFundNumber;

    @ExcelProperty("社保起缴月份")
    @Schema(description = "社保起缴月份")
    private LocalDateTime socialSecurityStartMonth;

    @ExcelProperty("参保方案名称")
    @Schema(description = "参保方案名称")
    private String schemeName;

    @ExcelProperty("备注")
    @Schema(description = "备注")
    private String remark;

}

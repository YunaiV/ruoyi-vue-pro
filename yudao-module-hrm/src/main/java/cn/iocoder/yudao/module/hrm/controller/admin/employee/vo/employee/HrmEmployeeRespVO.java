package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工档案 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class HrmEmployeeRespVO {

    @ExcelProperty(value = "员工编号", index = 0)
    @Schema(description = "员工档案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @ExcelProperty(value = "员工姓名", index = 1)
    @Schema(description = "员工姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @ExcelProperty(value = "工号", index = 2)
    @Schema(description = "工号", example = "HRM001")
    private String jobNumber;

    @Schema(description = "系统用户编号", example = "1024")
    private Long userId;

    @ExcelProperty(value = "绑定用户", index = 17)
    @Schema(description = "后台用户昵称", example = "芋道")
    private String userNickname;

    @ExcelProperty(value = "手机号", index = 3)
    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "国家地区", example = "中国")
    private String country;

    @Schema(description = "民族", example = "汉族")
    private String nation;

    @Schema(description = "证件类型", example = "1")
    private Integer idType;

    @Schema(description = "证件号码", example = "310101199001011234")
    private String idNumber;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "邮箱", example = "hrm@example.com")
    private String email;

    @Schema(description = "籍贯", example = "浙江杭州")
    private String nativePlace;

    @Schema(description = "出生时间")
    private LocalDateTime birthday;

    @Schema(description = "年龄", example = "28")
    private Integer age;

    @Schema(description = "户籍地址", example = "杭州市西湖区")
    private String address;

    @Schema(description = "最高学历", example = "8")
    private Integer highestEducation;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @ExcelProperty(value = "部门", index = 4)
    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "直属上级员工编号", example = "1")
    private Long leaderEmployeeId;

    @ExcelProperty(value = "直属上级", index = 7)
    @Schema(description = "直属上级员工姓名", example = "李四")
    private String leaderEmployeeName;

    @ExcelProperty(value = "入职状态", index = 8, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_ENTRY_STATUS)
    @Schema(description = "入职状态", example = "1")
    private Integer entryStatus;

    @ExcelProperty(value = "员工状态", index = 9, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_STATUS)
    @Schema(description = "员工状态", example = "1")
    private Integer status;

    @ExcelProperty(value = "聘用形式", index = 10, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_EMPLOYEE_TYPE)
    @Schema(description = "聘用形式", example = "1")
    private Integer type;

    @ExcelProperty(value = "入职时间", index = 11)
    @Schema(description = "入职时间")
    private LocalDateTime entryTime;

    @Schema(description = "试用期，单位月；0 表示无试用期", example = "3")
    private Integer probation;

    @ExcelProperty(value = "转正时间", index = 12)
    @Schema(description = "转正时间")
    private LocalDateTime regularTime;

    @ExcelProperty(value = "离职时间", index = 13)
    @Schema(description = "离职时间")
    private LocalDateTime leaveTime;

    @ExcelProperty(value = "岗位", index = 5)
    @Schema(description = "职位名称", example = "Java 工程师")
    private String postName;

    @ExcelProperty(value = "岗位职级", index = 6)
    @Schema(description = "岗位职级", example = "P6")
    private String postLevel;

    @ExcelProperty(value = "工作城市", index = 14)
    @Schema(description = "工作城市", example = "上海")
    private String workCity;

    @ExcelProperty(value = "工作地点", index = 15)
    @Schema(description = "工作地址", example = "浦东新区")
    private String workAddress;

    @Schema(description = "工作详细地址", example = "张江高科 1 号楼")
    private String workDetailAddress;

    @Schema(description = "招聘渠道编号", example = "10")
    private Long channelId;

    @ExcelProperty(value = "招聘渠道", index = 16)
    @Schema(description = "招聘渠道名称", example = "BOSS直聘")
    private String channelName;

    @Schema(description = "司龄开始时间")
    private LocalDateTime companyAgeStartTime;

    @Schema(description = "司龄，单位年", example = "2")
    private Integer companyAge;

    @Schema(description = "关联招聘候选人编号", example = "200")
    private Long candidateId;

    // ==================== 工资卡信息 ====================

    @Schema(description = "银行卡号", example = "622202600001")
    private String salaryCardNumber;

    @Schema(description = "开户地区编号", example = "440300")
    private Integer salaryCardAreaId;

    @Schema(description = "开户地区名称", example = "广东省 深圳市")
    private String salaryCardAreaName;

    @Schema(description = "银行名称", example = "招商银行")
    private String salaryCardBankName;

    @Schema(description = "开户支行名称", example = "科技园支行")
    private String salaryCardBankBranchName;

    // ==================== 社保信息 ====================

    @Schema(description = "个人社保账号", example = "SB20260001")
    private String socialSecurityNumber;

    @Schema(description = "个人公积金账号", example = "GJJ20260001")
    private String accumulationFundNumber;

    @Schema(description = "备注", example = "首批迁移员工")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

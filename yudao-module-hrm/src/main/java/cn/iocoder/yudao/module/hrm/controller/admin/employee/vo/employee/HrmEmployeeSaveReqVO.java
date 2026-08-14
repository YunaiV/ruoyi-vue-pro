package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeEducationEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeIdTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBeforeOrEqual;

@Schema(description = "管理后台 - HRM 员工档案新增/修改 Request VO")
@Data
public class HrmEmployeeSaveReqVO {

    @Schema(description = "员工档案编号", example = "1024")
    private Long id;

    @Schema(description = "员工姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 255, message = "员工姓名不能超过 255 个字符")
    private String name;

    @Schema(description = "工号", example = "HRM001")
    @Size(max = 64, message = "工号不能超过 64 个字符")
    private String jobNumber;

    @Schema(description = "系统用户编号", example = "1024")
    private Long userId;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotBlank(message = "手机号不能为空")
    @Mobile
    private String mobile;

    @Schema(description = "国家地区", example = "中国")
    @Size(max = 64, message = "国家地区不能超过 64 个字符")
    private String country;

    @Schema(description = "民族", example = "汉族")
    @Size(max = 64, message = "民族不能超过 64 个字符")
    private String nation;

    @Schema(description = "证件类型", example = "1")
    @InEnum(value = HrmEmployeeIdTypeEnum.class, message = "证件类型必须是 {value}")
    private Integer idType;

    @Schema(description = "证件号码", example = "310101199001011234")
    @Size(max = 255, message = "证件号码不能超过 255 个字符")
    private String idNumber;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "邮箱", example = "hrm@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱不能超过 255 个字符")
    private String email;

    @Schema(description = "籍贯", example = "浙江杭州")
    @Size(max = 128, message = "籍贯不能超过 128 个字符")
    private String nativePlace;

    @Schema(description = "出生时间")
    @PastOrPresent(message = "出生时间不能晚于当前时间")
    private LocalDateTime birthday;

    @Schema(description = "年龄", example = "28")
    private Integer age;

    @Schema(description = "户籍地址", example = "杭州市西湖区")
    @Size(max = 255, message = "户籍地址不能超过 255 个字符")
    private String address;

    @Schema(description = "最高学历", example = "8")
    @InEnum(value = HrmEmployeeEducationEnum.class, message = "最高学历必须是 {value}")
    private Integer highestEducation;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "直属上级员工编号", example = "1")
    private Long leaderEmployeeId;

    @Schema(description = "入职状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入职状态不能为空")
    @InEnum(value = HrmEmployeeEntryStatusEnum.class, message = "入职状态必须是 {value}")
    private Integer entryStatus;

    @Schema(description = "员工状态", example = "1")
    @InEnum(value = HrmEmployeeStatusEnum.class, message = "员工状态必须是 {value}")
    private Integer status;

    @Schema(description = "聘用形式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "聘用形式不能为空")
    @InEnum(value = HrmEmployeeTypeEnum.class, message = "聘用形式必须是 {value}")
    private Integer type;

    @Schema(description = "入职时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入职时间不能为空")
    private LocalDateTime entryTime;

    @Schema(description = "试用期，单位月；0 表示无试用期", example = "3")
    @Min(value = 0, message = "试用期不能小于 0 个月")
    @Max(value = 6, message = "试用期不能超过 6 个月")
    private Integer probation;

    @Schema(description = "转正时间")
    private LocalDateTime regularTime;

    @Schema(description = "离职时间")
    private LocalDateTime leaveTime;

    @Schema(description = "职位名称", example = "Java 工程师")
    @Size(max = 255, message = "职位名称不能超过 255 个字符")
    private String postName;

    @Schema(description = "岗位职级", example = "P6")
    @Size(max = 255, message = "岗位职级不能超过 255 个字符")
    private String postLevel;

    @Schema(description = "工作城市", example = "上海")
    @Size(max = 64, message = "工作城市不能超过 64 个字符")
    private String workCity;

    @Schema(description = "工作地址", example = "浦东新区")
    @Size(max = 255, message = "工作地址不能超过 255 个字符")
    private String workAddress;

    @Schema(description = "工作详细地址", example = "张江高科 1 号楼")
    @Size(max = 255, message = "工作详细地址不能超过 255 个字符")
    private String workDetailAddress;

    @Schema(description = "招聘渠道编号", example = "10")
    private Long channelId;

    @Schema(description = "司龄开始时间")
    private LocalDateTime companyAgeStartTime;

    @Schema(description = "司龄，单位年", example = "2")
    private Integer companyAge;

    @Schema(description = "关联招聘候选人编号", example = "200")
    private Long candidateId;

    @Schema(description = "备注", example = "首批迁移员工")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @AssertTrue(message = "身份证号码格式不正确")
    @JsonIgnore
    public boolean isIdNumberValid() {
        return ObjUtil.notEqual(HrmEmployeeIdTypeEnum.ID_CARD.getType(), idType)
                || StrUtil.isBlank(idNumber) || IdcardUtil.isValidCard(idNumber);
    }

    @AssertTrue(message = "员工状态与聘用形式不匹配")
    @JsonIgnore
    public boolean isStatusValid() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.INFORMAL.getType(), type)
                || status == null || HrmEmployeeStatusEnum.INFORMAL_STATUSES.contains(status);
    }

    @AssertTrue(message = "非正式员工状态不能为空")
    @JsonIgnore
    public boolean isStatusRequired() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.INFORMAL.getType(), type) || status != null;
    }

    @AssertTrue(message = "在职员工工号不能为空")
    @JsonIgnore
    public boolean isJobNumberValid() {
        return ObjUtil.notEqual(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus(), entryStatus)
                || StrUtil.isNotBlank(jobNumber);
    }

    @AssertTrue(message = "正式员工试用期不能为空")
    @JsonIgnore
    public boolean isProbationValid() {
        return ObjUtil.notEqual(HrmEmployeeTypeEnum.FORMAL.getType(), type) || probation != null;
    }

    @AssertTrue(message = "司龄起算时间不能晚于入职时间")
    @JsonIgnore
    public boolean isCompanyAgeStartTimeValid() {
        return entryTime == null || companyAgeStartTime == null
                || isBeforeOrEqual(companyAgeStartTime, entryTime);
    }

}

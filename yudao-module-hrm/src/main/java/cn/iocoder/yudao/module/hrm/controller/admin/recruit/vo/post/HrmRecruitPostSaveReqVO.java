package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post;

import cn.iocoder.yudao.module.hrm.framework.operatelog.core.HrmAreaParseFunction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO.AGE_UNLIMITED_VALUE;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO.SALARY_NEGOTIABLE_UNIT_VALUE;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO.SALARY_NEGOTIABLE_VALUE;

@Schema(description = "管理后台 - HRM 招聘职位新增/修改 Request VO")
@Data
public class HrmRecruitPostSaveReqVO {

    @Schema(description = "招聘职位编号", example = "1024")
    private Long id;

    @Schema(description = "职位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发工程师")
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 255, message = "职位名称不能超过 255 个字符")
    @DiffLogField(name = "职位名称")
    private String postName;

    @Schema(description = "部门编号", example = "100")
    @DiffLogField(name = "部门编号")
    private Long deptId;

    @Schema(description = "工作性质", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工作性质不能为空")
    @DiffLogField(name = "工作性质")
    private Integer jobNature;

    @Schema(description = "工作城市地区编号", example = "310115")
    @DiffLogField(name = "工作城市", function = HrmAreaParseFunction.NAME)
    private Integer areaId;

    @Schema(description = "招聘人数", example = "3")
    @Min(value = 0, message = "招聘人数不能小于 0")
    @DiffLogField(name = "招聘人数")
    private Integer recruitNum;

    @Schema(description = "招聘原因", example = "团队扩编")
    @Size(max = 255, message = "招聘原因不能超过 255 个字符")
    @DiffLogField(name = "招聘原因")
    private String reason;

    @Schema(description = "工作经验", example = "3")
    @DiffLogField(name = "工作经验")
    private Integer workTime;

    @Schema(description = "学历要求", example = "4")
    @DiffLogField(name = "学历要求")
    private Integer educationRequire;

    @Schema(description = "最低薪资", example = "20000")
    @Digits(integer = 8, fraction = 2, message = "最低薪资最多 8 位整数和 2 位小数")
    @DiffLogField(name = "最低薪资")
    private BigDecimal minSalary;

    @Schema(description = "最高薪资", example = "30000")
    @Digits(integer = 8, fraction = 2, message = "最高薪资最多 8 位整数和 2 位小数")
    @DiffLogField(name = "最高薪资")
    private BigDecimal maxSalary;

    @Schema(description = "薪资单位", example = "2")
    @DiffLogField(name = "薪资单位")
    private Integer salaryUnit;

    @Schema(description = "最小年龄", example = "22")
    @DiffLogField(name = "最小年龄")
    private Integer minAge;

    @Schema(description = "最大年龄", example = "35")
    @DiffLogField(name = "最大年龄")
    private Integer maxAge;

    @Schema(description = "最迟到岗时间")
    @DiffLogField(name = "最迟到岗时间")
    private LocalDateTime latestEntryTime;

    @Schema(description = "负责人员工编号", example = "1")
    @DiffLogField(name = "负责人")
    private Long ownerEmployeeId;

    @Schema(description = "面试官员工编号数组", example = "[1, 2]")
    @DiffLogField(name = "面试官")
    private List<Long> interviewEmployeeIds;

    @Schema(description = "职位描述", example = "负责核心业务系统设计与开发")
    @Size(max = 4000, message = "职位描述不能超过 4000 个字符")
    @DiffLogField(name = "职位描述")
    private String description;

    @Schema(description = "紧急程度", example = "1")
    @DiffLogField(name = "紧急程度")
    private Integer emergencyLevel;

    @Schema(description = "职位类型编号", example = "100")
    @DiffLogField(name = "职位类型编号")
    private Long postTypeId;

    @AssertTrue(message = "薪资范围不正确")
    @JsonIgnore
    public boolean isSalaryRangeValid() {
        boolean hasNegotiableValue = SALARY_NEGOTIABLE_VALUE.equals(getMinSalary())
                || SALARY_NEGOTIABLE_VALUE.equals(getMaxSalary()) || SALARY_NEGOTIABLE_UNIT_VALUE.equals(getSalaryUnit());
        if (hasNegotiableValue) {
            return SALARY_NEGOTIABLE_VALUE.equals(getMinSalary())
                    && SALARY_NEGOTIABLE_VALUE.equals(getMaxSalary())
                    && SALARY_NEGOTIABLE_UNIT_VALUE.equals(getSalaryUnit());
        }
        if ((getMinSalary() != null && getMinSalary().signum() < 0)
                || (getMaxSalary() != null && getMaxSalary().signum() < 0)) {
            return false;
        }
        return getMinSalary() == null || getMaxSalary() == null || getMinSalary().compareTo(getMaxSalary()) <= 0;
    }

    @AssertTrue(message = "年龄范围不正确")
    @JsonIgnore
    public boolean isAgeRangeValid() {
        boolean hasUnlimitedValue = AGE_UNLIMITED_VALUE.equals(getMinAge()) || AGE_UNLIMITED_VALUE.equals(getMaxAge());
        if (hasUnlimitedValue) {
            return AGE_UNLIMITED_VALUE.equals(getMinAge()) && AGE_UNLIMITED_VALUE.equals(getMaxAge());
        }
        if ((getMinAge() != null && getMinAge() < 0) || (getMaxAge() != null && getMaxAge() < 0)) {
            return false;
        }
        return getMinAge() == null || getMaxAge() == null || getMinAge() <= getMaxAge();
    }

}

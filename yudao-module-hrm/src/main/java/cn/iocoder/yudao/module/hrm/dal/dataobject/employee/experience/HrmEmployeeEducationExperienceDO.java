package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeEducationEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeTeachingMethodEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * HRM 员工教育经历 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_education_experience")
@KeySequence("hrm_employee_education_experience_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeEducationExperienceDO extends BaseDO {

    /**
     * 教育经历编号
     */
    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 学历
     *
     * 枚举 {@link HrmEmployeeEducationEnum}
     * 字典 {@link DictTypeConstants#HRM_EMPLOYEE_EDUCATION}
     */
    private Integer education;
    /**
     * 毕业院校
     */
    private String graduateSchool;
    /**
     * 专业
     */
    private String major;
    /**
     * 入学日期
     */
    private LocalDateTime admissionTime;
    /**
     * 毕业日期
     */
    private LocalDateTime graduationTime;
    /**
     * 教学方式
     *
     * 枚举 {@link HrmEmployeeTeachingMethodEnum}
     */
    private Integer teachingMethods;
    /**
     * 是否第一学历
     *
     */
    private Boolean firstDegree;
    /**
     * 显示顺序
     */
    private Integer sort;

}

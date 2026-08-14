package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * HRM 薪资组 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_group", autoResultMap = true)
@KeySequence("hrm_salary_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryGroupDO extends BaseDO {

    /**
     * 默认月计薪天数
     */
    public static final BigDecimal DEFAULT_SALARY_STANDARD = BigDecimal.valueOf(21.75);
    /**
     * 默认薪资变更计算规则
     */
    public static final String DEFAULT_CHANGE_RULE = "按转正/调薪前后的工资混合计算";

    @TableId
    private Long id;
    /**
     * 薪资组名称
     */
    private String name;
    /**
     * 月计薪标准
     */
    private BigDecimal salaryStandard;
    /**
     * 转正、调薪月规则
     */
    private String changeRule;
    /**
     * 适用部门编号列表
     *
     * 关联 {@link DeptDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> deptIds;
    /**
     * 适用员工编号列表
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> employeeIds;
    /**
     * 计税规则编号
     *
     * 关联 {@link HrmSalaryTaxRuleDO#getId()}
     */
    private Long taxRuleId;

}

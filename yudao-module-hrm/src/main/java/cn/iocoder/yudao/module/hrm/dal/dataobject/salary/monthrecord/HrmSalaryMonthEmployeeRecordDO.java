package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * HRM 员工月度工资记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_month_employee_record", autoResultMap = true)
@KeySequence("hrm_salary_month_employee_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryMonthEmployeeRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 月度工资表编号
     *
     * 关联 {@link HrmSalaryMonthRecordDO#getId()}
     */
    private Long monthRecordId;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 实际工作天数
     */
    private BigDecimal actualWorkDay;
    /**
     * 应工作天数
     */
    private BigDecimal needWorkDay;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 应发工资
     */
    private BigDecimal expectedPaySalary;
    /**
     * 应税工资
     */
    private BigDecimal taxableSalary;
    /**
     * 个人所得税
     */
    private BigDecimal personalTax;
    /**
     * 实发工资
     */
    private BigDecimal realPaySalary;

    /**
     * 薪资项值快照
     *
     * 薪资项值随员工月度工资记录整体维护
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<OptionValue> optionValues;

    /**
     * 薪资项值
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValue {

        /**
         * 薪资项编码
         *
         * 关联 {@link HrmSalaryOptionDO#getCode()}
         */
        private Integer code;
        /**
         * 薪资项名称
         *
         * 关联 {@link HrmSalaryOptionDO#getName()}
         */
        private String name;
        /**
         * 薪资项金额
         */
        private BigDecimal value;
    }

}

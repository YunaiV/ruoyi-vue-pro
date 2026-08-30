package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipReadStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
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
 * HRM 工资条 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_slip", autoResultMap = true)
@KeySequence("hrm_salary_slip_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalarySlipDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工资条发放记录编号
     *
     * 关联 {@link HrmSalarySlipSendRecordDO#getId()}
     */
    private Long sendRecordId;
    /**
     * 员工月度工资记录编号
     *
     * 关联 {@link HrmSalaryMonthEmployeeRecordDO#getId()}
     */
    private Long monthEmployeeRecordId;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 阅读状态
     *
     * 枚举 {@link HrmSalarySlipReadStatusEnum}
     */
    private Integer readStatus;
    /**
     * 实发工资
     */
    private BigDecimal realPaySalary;
    /**
     * 备注
     */
    private String remark;
    /**
     * 工资条项
     *
     * 工资条项随员工工资条整体维护，通过 {@link Option#getChildren()} 表达父子关系
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Option> options;

    /**
     * 工资条项快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {

        /**
         * 名称
         *
         * 关联 {@link HrmSalaryOptionDO#getName()}
         */
        private String name;
        /**
         * 类型
         *
         * 枚举 {@link HrmSalarySlipTemplateOptionTypeEnum}
         */
        private Integer type;
        /**
         * 薪资项编码
         *
         * 关联 {@link HrmSalaryOptionDO#getCode()}
         */
        private Integer code;
        /**
         * 金额
         */
        private BigDecimal value;
        /**
         * 说明
         */
        private String remark;
        /**
         * 排序
         *
         * 来源 {@link HrmSalarySlipTemplateDO.Option#getSort()}
         */
        private Integer sort;
        /**
         * 子项
         */
        private List<Option> children;
    }

}

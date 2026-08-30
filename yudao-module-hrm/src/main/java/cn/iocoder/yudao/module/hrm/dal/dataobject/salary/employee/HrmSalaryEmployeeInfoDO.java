package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeInfoChangeTypeEnum;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 员工薪资信息 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_employee_info", autoResultMap = true)
@KeySequence("hrm_salary_employee_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryEmployeeInfoDO extends BaseDO {

    /**
     * 编号
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
     * 定薪、调薪原因
     *
     * 枚举 {@link HrmSalaryChangeReasonEnum}
     */
    private Integer changeReason;
    /**
     * 生效时间
     */
    private LocalDateTime effectTime;
    /**
     * 薪资信息变更类型
     *
     * 枚举 {@link HrmSalaryEmployeeInfoChangeTypeEnum}
     */
    private Integer changeType;
    /**
     * 试用期工资
     */
    private BigDecimal probationSalary;
    /**
     * 正式工资
     */
    private BigDecimal regularSalary;
    /**
     * 备注
     */
    private String remark;
    /**
     * 正式薪资项
     *
     * 薪资项随员工薪资信息整体维护
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<SalaryOption> salaryOptions;
    /**
     * 试用期薪资项
     *
     * 薪资项随员工薪资信息整体维护
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<SalaryOption> probationSalaryOptions;

    /**
     * 薪资项快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryOption {

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

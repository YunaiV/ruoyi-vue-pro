package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 定薪/调薪记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_change_record", autoResultMap = true)
@KeySequence("hrm_salary_change_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryChangeRecordDO extends BaseDO {

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
     * 记录类型
     *
     * 枚举 {@link HrmSalaryChangeRecordTypeEnum}
     */
    private Integer type;
    /**
     * 调整原因
     *
     * 枚举 {@link HrmSalaryChangeReasonEnum}
     */
    private Integer reason;
    /**
     * 生效时间
     */
    private LocalDateTime effectTime;
    /**
     * 调整前正式薪资
     */
    private BigDecimal beforeTotal;
    /**
     * 调整后正式薪资
     */
    private BigDecimal afterTotal;
    /**
     * 调整前试用期薪资
     */
    private BigDecimal probationBeforeTotal;
    /**
     * 调整后试用期薪资
     */
    private BigDecimal probationAfterTotal;
    /**
     * 正式薪资项后态快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SalaryOption> salaryOptions;
    /**
     * 试用期薪资项后态快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SalaryOption> probationSalaryOptions;
    /**
     * 状态
     *
     * 枚举 {@link HrmSalaryChangeRecordStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

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

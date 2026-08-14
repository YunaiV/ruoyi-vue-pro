package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
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
 * HRM 月度工资表 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_month_record", autoResultMap = true)
@KeySequence("hrm_salary_month_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryMonthRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工资表标题
     */
    private String title;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 员工人数
     */
    private Integer employeeCount;
    /**
     * 计薪开始日期
     */
    private LocalDateTime startTime;
    /**
     * 计薪结束日期
     */
    private LocalDateTime endTime;
    /**
     * 应发工资
     */
    private BigDecimal expectedPaySalary;
    /**
     * 个人所得税
     */
    private BigDecimal personalTax;
    /**
     * 实发工资
     */
    private BigDecimal realPaySalary;
    /**
     * 个人社保金额
     */
    private BigDecimal personalInsuranceAmount;
    /**
     * 个人公积金金额
     */
    private BigDecimal personalProvidentFundAmount;
    /**
     * 公司社保金额
     */
    private BigDecimal corporateInsuranceAmount;
    /**
     * 公司公积金金额
     */
    private BigDecimal corporateProvidentFundAmount;
    /**
     * 薪资项表头快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<OptionHeader> optionHeaders;
    /**
     * 状态
     *
     * 枚举 {@link HrmSalaryMonthRecordStatusEnum}
     */
    private Integer status;
    /**
     * 薪资项表头快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionHeader {

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
         * 子薪资项
         */
        private List<OptionHeader> children;

    }

}

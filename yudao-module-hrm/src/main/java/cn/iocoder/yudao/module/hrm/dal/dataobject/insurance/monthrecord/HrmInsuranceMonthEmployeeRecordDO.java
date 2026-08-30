package cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord;

import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
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
 * HRM 员工月度社保记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_insurance_month_employee_record", autoResultMap = true)
@KeySequence("hrm_insurance_month_employee_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmInsuranceMonthEmployeeRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 月度社保表编号
     *
     * 关联 {@link HrmInsuranceMonthRecordDO#getId()}
     */
    private Long monthRecordId;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 社保方案编号
     *
     * 关联 {@link HrmInsuranceSchemeDO#getId()}
     */
    private Long schemeId;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
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
     * 参保状态
     *
     * 枚举 {@link HrmInsuranceEmployeeStatusEnum}
     */
    private Integer status;
    /**
     * 参保项目快照
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Project> projects;

    /**
     * 参保项目快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {

        /**
         * 来源社保方案项目编号
         *
         * 关联 {@link HrmInsuranceSchemeProjectDO#getId()}
         */
        private Long schemeProjectId;
        /**
         * 项目类型
         *
         * 枚举 {@link HrmInsuranceProjectTypeEnum}
         */
        private Integer type;
        /**
         * 项目名称
         */
        private String name;
        /**
         * 缴纳基数
         */
        private BigDecimal baseAmount;
        /**
         * 公司缴纳比例
         */
        private BigDecimal corporateRate;
        /**
         * 个人缴纳比例
         */
        private BigDecimal personalRate;
        /**
         * 公司缴纳金额
         */
        private BigDecimal corporateAmount;
        /**
         * 个人缴纳金额
         */
        private BigDecimal personalAmount;

    }

}

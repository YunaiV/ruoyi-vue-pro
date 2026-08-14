package cn.iocoder.yudao.module.fms.dal.dataobject.closing;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
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
 * FMS 结账方案 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_closing", autoResultMap = true)
@KeySequence("fms_closing_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsClosingSchemeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 结账名称
     */
    private String name;
    /**
     * 是否期末结转
     */
    private Boolean periodEnd;
    /**
     * 科目编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 取数规则
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum}
     */
    private Integer formulaRule;
    /**
     * 取数时间类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum}
     */
    private Integer timeType;
    /**
     * 凭证字编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO#getId()}
     */
    private Long voucherWordId;
    /**
     * 凭证摘要
     */
    private String digest;
    /**
     * 结转凭证类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.closing.FmsClosingVoucherTypeEnum}
     */
    private Integer voucherType;
    /**
     * “以前年度损益调整”科目
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
     */
    private Long priorYearAdjustmentSubjectId;
    /**
     * “以前年度损益调整”科目的结转科目
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
     */
    private Long adjustmentClosingSubjectId;
    /**
     * 其他损益科目的结转科目
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
     */
    private Long otherClosingSubjectId;
    /**
     * 是否按余额反向结转
     */
    private Boolean reverseBalance;
    /**
     * 类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum}
     */
    private Integer type;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 结转损益日期，最大31，超过31默认最后一天
     */
    private Integer closingDay;
    /**
     * 结转科目规则数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SubjectRule> subjectRules;

    /**
     * 结转科目规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectRule {

        /**
         * 科目编号
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getId()}
         */
        private Long subjectId;
        /**
         * 科目编码快照
         *
         * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO#getCode()}
         */
        private String subjectCode;
        /**
         * 摘要
         */
        private String digest;
        /**
         * 借贷方向
         *
         * 枚举 {@link FmsDebitCreditDirectionEnum}
         */
        private Integer direction;
        /**
         * 金额比例，取值范围 0 至 100
         */
        private BigDecimal amountRatio;
    }

}

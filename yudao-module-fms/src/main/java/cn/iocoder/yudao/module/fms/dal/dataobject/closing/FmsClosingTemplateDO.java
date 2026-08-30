package cn.iocoder.yudao.module.fms.dal.dataobject.closing;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
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
 * FMS 结账模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_closing_template", autoResultMap = true)
@KeySequence("fms_closing_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsClosingTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 系统预置编码
     */
    private String presetCode;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板分类
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTemplateCategoryEnum}
     */
    private Integer category;
    /**
     * 是否期末结转
     */
    private Boolean periodEnd;
    /**
     * 来源科目编号
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
     * 枚举 {@link FmsClosingTimeTypeEnum}
     */
    private Integer timeType;
    /**
     * 结转科目规则数组
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<SubjectRule> subjectRules;
    /**
     * 显示顺序
     */
    private Integer sort;

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

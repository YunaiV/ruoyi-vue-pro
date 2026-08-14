package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaScoreTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceQuotaTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceScoreCalculationEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceUpperLimitTypeEnum;
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
 * HRM 绩效考核模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_performance_assessment_template", autoResultMap = true)
@KeySequence("hrm_performance_assessment_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmPerformanceAssessmentTemplateDO extends BaseDO {

    /**
     * 绩效考核模板编号
     */
    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板说明
     */
    private String illustrate;
    /**
     * 计分方式
     *
     * 枚举 {@link HrmPerformanceScoreCalculationEnum}
     */
    private Integer scoreCalculation;
    /**
     * 分数上限类型
     *
     * 枚举 {@link HrmPerformanceUpperLimitTypeEnum}
     */
    private Integer upperLimitType;
    /**
     * 分数上限
     */
    private BigDecimal upperLimitScore;
    /**
     * 维度数量
     */
    private Integer dimensionCount;
    /**
     * 指标数量
     */
    private Integer quotaCount;
    /**
     * 考核维度配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Dimension> dimensions;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 绩效考核配置快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentConfig {

        /**
         * 模板名称
         */
        private String name;
        /**
         * 计分方式
         *
         * 枚举 {@link HrmPerformanceScoreCalculationEnum}
         */
        private Integer scoreCalculation;
        /**
         * 分数上限类型
         *
         * 枚举 {@link HrmPerformanceUpperLimitTypeEnum}
         */
        private Integer upperLimitType;
        /**
         * 分数上限
         */
        private BigDecimal upperLimitScore;
        /**
         * 考核维度配置
         */
        private List<Dimension> dimensions;
    }

    /**
     * 绩效考核维度
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimension {

        /**
         * 维度名称
         */
        private String name;
        /**
         * 指标类型
         *
         * 枚举 {@link HrmPerformanceQuotaTypeEnum}
         */
        private Integer quotaType;
        /**
         * 维度权重
         */
        private BigDecimal weight;
        /**
         * 备注
         */
        private String remark;
        /**
         * 是否允许员工编辑
         */
        private Boolean allowEdit;
        /**
         * 考核指标配置
         */
        private List<Quota> quotas;
    }

    /**
     * 绩效考核指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quota {

        /**
         * 指标名称
         */
        private String name;
        /**
         * 指标说明
         */
        private String illustrate;
        /**
         * 评分标准
         */
        private String standard;
        /**
         * 指标权重
         */
        private BigDecimal weight;
        /**
         * 评分类型
         *
         * 枚举 {@link HrmPerformanceQuotaScoreTypeEnum}
         */
        private Integer scoreType;
    }

}

package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
 * HRM 绩效结果模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_performance_result_template", autoResultMap = true)
@KeySequence("hrm_performance_result_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmPerformanceResultTemplateDO extends BaseDO {

    /**
     * 绩效结果模板编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 结果等级配置
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Level> levels;

    /**
     * 绩效结果等级
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Level {

        /**
         * 等级名称
         */
        private String name;
        /**
         * 最低分数
         */
        private BigDecimal minScore;
        /**
         * 最高分数
         */
        private BigDecimal maxScore;
        /**
         * 绩效系数
         */
        private BigDecimal coefficient;
    }

}

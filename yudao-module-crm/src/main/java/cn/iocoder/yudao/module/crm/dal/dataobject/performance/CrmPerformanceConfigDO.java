package cn.iocoder.yudao.module.crm.dal.dataobject.performance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * CRM 业绩目标 DO
 *
 * @author 芋道源码
 */
@TableName("crm_performance_config")
@KeySequence("crm_performance_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmPerformanceConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 目标类型
     *
     * 枚举 {@link CrmPerformanceConfigBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 目标对象编号
     */
    private Long objectId;
    /**
     * 目标对象类型
     *
     * 枚举 {@link CrmPerformanceConfigObjectTypeEnum}
     */
    private Integer objectType;

    /**
     * 年份
     */
    private Integer year;
    /**
     * 年度目标金额
     */
    private BigDecimal yearTargetPrice;
    /**
     * 一月目标金额
     */
    private BigDecimal januaryTargetPrice;
    /**
     * 二月目标金额
     */
    private BigDecimal februaryTargetPrice;
    /**
     * 三月目标金额
     */
    private BigDecimal marchTargetPrice;
    /**
     * 四月目标金额
     */
    private BigDecimal aprilTargetPrice;
    /**
     * 五月目标金额
     */
    private BigDecimal mayTargetPrice;
    /**
     * 六月目标金额
     */
    private BigDecimal juneTargetPrice;
    /**
     * 七月目标金额
     */
    private BigDecimal julyTargetPrice;
    /**
     * 八月目标金额
     */
    private BigDecimal augustTargetPrice;
    /**
     * 九月目标金额
     */
    private BigDecimal septemberTargetPrice;
    /**
     * 十月目标金额
     */
    private BigDecimal octoberTargetPrice;
    /**
     * 十一月目标金额
     */
    private BigDecimal novemberTargetPrice;
    /**
     * 十二月目标金额
     */
    private BigDecimal decemberTargetPrice;

}

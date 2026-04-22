package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设计成本预估表 — CostEstimateAgent 落库，供 PricingStrategyAgent 使用历史均价。
 */
@Data
@TableName("deepay_design_cost")
public class DeepayDesignCostDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 面料成本（元） */
    private BigDecimal fabricCost;

    /** 人工成本（元） */
    private BigDecimal laborCost;

    /** 总成本（元）= fabricCost + laborCost */
    private BigDecimal totalCost;

    /** 建议售价（元）= totalCost × 利润倍率 */
    private BigDecimal suggestPrice;

    /** 毛利率（suggest - cost）/ suggest */
    private BigDecimal margin;

    private LocalDateTime createdAt;
}

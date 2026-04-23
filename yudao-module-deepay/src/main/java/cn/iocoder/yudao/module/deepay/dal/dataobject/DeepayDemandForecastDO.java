package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求预测表 deepay_demand_forecast（Phase 5 — DemandAgent 输出）
 */
@TableName("deepay_demand_forecast")
@Data
public class DeepayDemandForecastDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 品类关键词（预测依据） */
    private String category;

    /** 预测销量（件数，未来 forecastDays 天） */
    private Integer predictedSales;

    /** 预测置信度（0.00~1.00） */
    private BigDecimal confidence;

    /** 预测周期（天），默认 7 */
    private Integer forecastDays;

    /** 季节因子（0.5 淡季 ~ 2.0 旺季） */
    private BigDecimal seasonFactor;

    /** 建议生产量 = predictedSales × safetyFactor */
    private Integer suggestedProductionQty;

    /** 预测创建时间 */
    private LocalDateTime createdAt;

}

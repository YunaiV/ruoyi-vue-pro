package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标表 deepay_metrics
 */
@TableName("deepay_metrics")
@Data
public class DeepayMetricsDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 销量快照 */
    private Integer soldCount;

    /** 上架价格快照 */
    private BigDecimal price;

    /** 分类（来自 keyword） */
    private String category;

    /** 商品详情页浏览次数 */
    private Integer viewCount;

    /** 成功支付次数（与 soldCount 对比可得转化率） */
    private Integer payCount;

    /** 转化率（pay_count / view_count），取值 0.00~1.00 */
    private BigDecimal conversionRate;

    /** 生产成本快照 */
    private BigDecimal costPrice;

    /** 单笔利润（price - cost_price） */
    private BigDecimal profit;

    /** 投资回报率（profit / cost_price） */
    private BigDecimal roi;

    /**
     * 风格标签（SEXY / CASUAL / SPORT / MINIMAL / LUXURY）。
     * 由 AnalyticsAgent 在落库时从 Context.style 写入，供 TrendAgent 风格加权排序使用。
     */
    private String style;

    private LocalDateTime createdAt;

}

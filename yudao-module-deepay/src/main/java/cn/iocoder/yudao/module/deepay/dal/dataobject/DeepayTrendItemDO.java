package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 外部趋势款选品表 deepay_trend_item（Phase 9 SelectionFeed）
 *
 * <p>由 SelectionFeedAgent 读取，将外部平台（1688 / TikTok / SHEIN / 品牌）
 * 的热门款式注入 {@link cn.iocoder.yudao.module.deepay.agent.Context#trendItems}，
 * 供 TrendAgent 与 AIDecisionAgent 排序消费。</p>
 */
@TableName("deepay_trend_item")
@Data
public class DeepayTrendItemDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 款式图片 URL（CDN 或外部链接） */
    private String imageUrl;

    /** 品牌名称 */
    private String brand;

    /** 品类（外套 / 内裤 / 连衣裙 / 裤子 / 上衣等） */
    private String category;

    /**
     * 风格标签。
     * 合法值：SEXY / MINIMAL / CASUAL / SPORT / LUXURY（大写）。
     */
    private String style;

    /** 参考售价（元） */
    private BigDecimal price;

    /**
     * 数据来源。
     * 合法值：1688 / tiktok / shein / brand
     */
    private String source;

    /** 热度分值（越高越流行，SelectionFeedAgent 按此降序取 Top N） */
    private Integer heatScore;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

}

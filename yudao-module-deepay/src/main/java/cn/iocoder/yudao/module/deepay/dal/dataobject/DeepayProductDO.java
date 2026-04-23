package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deepay 商品表。
 *
 * <p>对应数据库表 {@code deepay_product}。
 * 由 ProductAgent 生成，经 PricingAgent 定价，PublishAgent 写入。</p>
 */
@TableName("deepay_product")
@Data
public class DeepayProductDO {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的样式链码 */
    private String chainCode;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 商品封面图 URL */
    private String coverImage;

    /** 售价（使用 BigDecimal，禁止 double/float，货币单位 EUR） */
    private BigDecimal price;

    /**
     * 基准价（EUR）— AI 定价系统唯一输出，永远用 EUR 存储。
     * 展示时通过 {@link cn.iocoder.yudao.module.deepay.service.FxRateService#convert} 转换。
     * <pre>
     * basePrice = totalCost × 2.2 + trendBoost + marketAdjust
     * </pre>
     */
    private BigDecimal basePrice;

    /**
     * 货币代码（ISO 4217），全系统统一 EUR（欧元）。
     * 前端展示、Jeepay 创建支付均使用此字段。
     */
    private String currency = "EUR";

    /**
     * 商品状态。
     * <ul>
     *   <li>{@code SELLING} —— 在售</li>
     *   <li>{@code STOPPED} —— 已停售</li>
     *   <li>{@code REDESIGNING} —— 重设计中</li>
     * </ul>
     */
    private String status;

    /** 销量 */
    private Integer soldCount;

    /** 可用库存 */
    private Integer stock;

    /** 生产成本（元），用于计算利润和 ROI */
    private java.math.BigDecimal costPrice;

    /** CDN 图片地址（FLUX 生成后同步至 CDN） */
    private String cdnImageUrl;

    /**
     * 商品品类（外套 / 内裤 / 裤子 / 上衣 / 连衣裙 …）。
     * 由 ProductAgent 落库时写入（来自 Context.category）；
     * TrendAgent 用 WHERE category=? 做精准过滤。
     */
    private String category;

    /**
     * 发布渠道（逗号分隔，e.g. "H5" / "H5,1688" / "H5,Shopify"）。
     * PublishChannelAgent 落库时写入。
     */
    private String channel;

    /**
     * 主图（便捷字段，等价于 cdnImageUrl）。
     * TrendAgent 通过 {@link #getMainImage()} 读取，避免字段名不一致问题。
     */
    public String getMainImage() {
        return this.cdnImageUrl != null ? this.cdnImageUrl : this.coverImage;
    }
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;

}

package cn.iocoder.yudao.module.deepay.agent;

/**
 * TrendItem — 趋势商品值对象（Phase 6 TrendAgent 输出单元）。
 *
 * <p>由 {@link TrendAgent} 从 deepay_metrics JOIN deepay_product 查询组装，
 * 写入 {@link Context#trendItems}，供 StyleEngine / DesignAgent 消费。</p>
 */
public class TrendItem {

    /** CDN 图片地址（来自 deepay_product.cdn_image_url，可能为空） */
    private String imageUrl;

    /** 品类（来自 deepay_metrics.category） */
    private String category;

    /**
     * 风格标签（来自 deepay_metrics.style）。
     * 合法值：SEXY / CASUAL / SPORT / MINIMAL / LUXURY（大写）。
     */
    private String style;

    /** 近期销量（来自 deepay_metrics.sold_count），用于排序 */
    private Integer soldCount;

    /** 关联链码（来自 deepay_metrics.chain_code） */
    private String chainCode;

    /** 数据来源（1688 / TikTok / Shein / internal 等） */
    private String source;

    /** 综合评分（TrendAgent 多源评分，越高越优先展示） */
    private double score;

    // ---- constructors ----

    public TrendItem() {}

    public TrendItem(String imageUrl, String category, String style, Integer soldCount, String chainCode) {
        this.imageUrl  = imageUrl;
        this.category  = category;
        this.style     = style;
        this.soldCount = soldCount;
        this.chainCode = chainCode;
    }

    // ---- getters / setters ----

    public String getImageUrl()  { return imageUrl; }
    public void   setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    public String getCategory()  { return category; }
    public void   setCategory(String category)   { this.category = category; }

    public String getStyle()     { return style; }
    public void   setStyle(String style)         { this.style = style; }

    public Integer getSoldCount() { return soldCount; }
    public void    setSoldCount(Integer soldCount) { this.soldCount = soldCount; }

    public String getChainCode() { return chainCode; }
    public void   setChainCode(String chainCode) { this.chainCode = chainCode; }

    public String getSource() { return source; }
    public void   setSource(String source) { this.source = source; }

    public double getScore() { return score; }
    public void   setScore(double score) { this.score = score; }

}

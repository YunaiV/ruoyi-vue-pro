package cn.iocoder.yudao.module.deepay.agent;

/**
 * ProductDNA — 爆款结构拆解值对象（Phase 8 Hot Clone Engine）。
 *
 * <p>将一个爆款商品拆解为 7 个维度的结构化参数，
 * 每个维度可独立变化，通过排列组合生成变体款式。</p>
 *
 * <p>示例（外套爆款）：
 * <pre>
 *   category = "外套"
 *   fit      = "宽松"
 *   length   = "中长款"
 *   collar   = "翻领"
 *   color    = "黑"      ← 变化维度 1
 *   fabric   = "羊毛"    ← 变化维度 2
 *   style    = "MINIMAL" ← 保持与父款相同
 * </pre>
 * </p>
 */
public class ProductDNA {

    /** 品类（外套 / 内裤 / 裤子 / 上衣 / 连衣裙 …），与父款相同 */
    public String category;

    /** 版型（宽松 / 修身 / 直筒 / 标准） */
    public String fit;

    /** 长度（短款 / 中长款 / 长款） */
    public String length;

    /** 领型（立领 / 翻领 / 无领 / V领 / 圆领） */
    public String collar;

    /** 颜色（黑 / 白 / 灰 / 米白 / 深蓝 / 卡其 …），主要变化维度 */
    public String color;

    /** 面料（棉 / 牛仔 / 针织 / 羊毛 / 雪纺 / 莫代尔 …），主要变化维度 */
    public String fabric;

    /** 风格标签（SEXY / MINIMAL / CASUAL / SPORT / LUXURY），与父款相同 */
    public String style;

    public ProductDNA() {}

    public ProductDNA(String category, String fit, String length,
                      String collar, String color, String fabric, String style) {
        this.category = category;
        this.fit      = fit;
        this.length   = length;
        this.collar   = collar;
        this.color    = color;
        this.fabric   = fabric;
        this.style    = style;
    }

    /** 浅拷贝，用于生成变体时保持父款固定字段不变。 */
    public ProductDNA copy() {
        return new ProductDNA(category, fit, length, collar, color, fabric, style);
    }

    /**
     * 将 DNA 转为中文 Flux Prompt 片段。
     *
     * <p>示例输出：{@code 设计一款 外套，黑色，羊毛 面料，宽松 版型，中长款，翻领，MINIMAL 风格}</p>
     */
    public String toPrompt() {
        StringBuilder sb = new StringBuilder("设计一款 ");
        if (category != null) sb.append(category);
        if (color    != null) sb.append("，").append(color).append("色");
        if (fabric   != null) sb.append("，").append(fabric).append(" 面料");
        if (fit      != null) sb.append("，").append(fit).append(" 版型");
        if (length   != null) sb.append("，").append(length);
        if (collar   != null) sb.append("，").append(collar);
        if (style    != null) sb.append("，").append(style).append(" 风格");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ProductDNA{category=" + category + ", fit=" + fit + ", length=" + length
                + ", collar=" + collar + ", color=" + color + ", fabric=" + fabric
                + ", style=" + style + "}";
    }

}

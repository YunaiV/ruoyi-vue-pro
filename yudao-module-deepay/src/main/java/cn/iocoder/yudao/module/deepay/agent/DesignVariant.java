package cn.iocoder.yudao.module.deepay.agent;

/**
 * 设计变体 VO — DesignVariantAgent 输出的单个设计版本。
 *
 * <p>每次生成至少 3 个变体：
 * <ul>
 *   <li>variant1 — 原版微调（保留核心款，轻改细节）</li>
 *   <li>variant2 — 风格强化（更运动 / 更性感 / 更极简）</li>
 *   <li>variant3 — 简化版（去掉复杂结构，好卖款）</li>
 * </ul>
 * </p>
 */
public class DesignVariant {

    /** AI 生成图片 URL */
    private String imageUrl;

    /** 风格标签（偏欧美 / 偏基础 / 偏运动 / 风格强化 / 简化版） */
    private String styleTag;

    /**
     * 变体综合评分（0~100）。
     * ScoreUtil.computeDesignScore() 输出：
     * originality×0.3 + producibility×0.3 + costScore×0.2 + marketMatch×0.2
     */
    private Integer score;

    /** true = 被 DesignVariantAgent 自动选为最优（score 最高）。 */
    private boolean selected;

    /** 生成本变体时使用的完整 Prompt（记录备查）。 */
    private String prompt;

    // ----------------------------------------------------------------
    // 构造
    // ----------------------------------------------------------------

    public DesignVariant() {}

    public DesignVariant(String imageUrl, String styleTag, Integer score, String prompt) {
        this.imageUrl = imageUrl;
        this.styleTag = styleTag;
        this.score    = score;
        this.prompt   = prompt;
    }

    // ----------------------------------------------------------------
    // getters / setters
    // ----------------------------------------------------------------

    public String  getImageUrl()            { return imageUrl; }
    public void    setImageUrl(String v)    { this.imageUrl = v; }

    public String  getStyleTag()            { return styleTag; }
    public void    setStyleTag(String v)    { this.styleTag = v; }

    public Integer getScore()               { return score; }
    public void    setScore(Integer v)      { this.score = v; }

    public boolean isSelected()             { return selected; }
    public void    setSelected(boolean v)   { this.selected = v; }

    public String  getPrompt()              { return prompt; }
    public void    setPrompt(String v)      { this.prompt = v; }

    @Override
    public String toString() {
        return "DesignVariant{styleTag='" + styleTag + "', score=" + score
                + ", selected=" + selected + ", url='" + shorten(imageUrl) + "'}";
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 50 ? s.substring(0, 50) + "…" : s;
    }
}

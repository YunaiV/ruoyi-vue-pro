package cn.iocoder.yudao.module.deepay.agent;

import java.util.List;

/**
 * 风格组合方向 — StyleEngine.buildCombinations() 的输出单元。
 *
 * <p>每个 StyleCombo 代表一个具体的设计选款方向，包含：
 * <ul>
 *   <li>主风格 + 副风格组合（两种不同风格的叠加）</li>
 *   <li>参考品牌列表（给设计师参考，不是要卖这些品牌）</li>
 *   <li>完整 Prompt（可直接传给 FluxService / DesignAgent）</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 *   category=外套, style=工装, crowd=男装, market=EU
 *   → id=1, primary=工装, secondary=极简, sourceRefs=[Balenciaga, ZARA], tier=HOT
 *   → id=2, primary=工装, secondary=街头, sourceRefs=[Nike, Adidas],      tier=HOT
 *   → id=3, primary=工装, secondary=轻奢, sourceRefs=[Gucci, Prada],       tier=DESIGN
 *   ...（共 10~20 个方向）
 * </pre>
 * </p>
 */
public class StyleCombo {

    /** 序号（1 起始） */
    private int id;

    /** 品类（来自 ctx.category） */
    private String category;

    /** 主风格（客户偏好风格） */
    private String primaryStyle;

    /** 副风格（与主风格互补的风格，丰富设计方向） */
    private String secondaryStyle;

    /** 客群（来自 ctx.crowd） */
    private String crowd;

    /** 市场（来自 ctx.market） */
    private String market;

    /**
     * 数据来源层级：
     * <ul>
     *   <li>HOT    — 爆款层（1688/TikTok/SHEIN，大众款，优先推）</li>
     *   <li>DESIGN — 设计层（runway/brand，灵感款，搭配推）</li>
     * </ul>
     */
    private String tier;

    /**
     * 参考品牌列表（给设计师看"大概这个方向的代表品牌"）
     * 例如：["ZARA", "Balenciaga"]、["Nike", "Adidas"]
     */
    private List<String> sourceRefs;

    /**
     * 完整设计 Prompt（可直接传 FluxService/DesignAgent，已拼好 category + style + crowd + market）
     * 例如："设计一款外套，工装+极简 风格，男装，欧洲（EU）市场，参考：Balenciaga、ZARA"
     */
    private String prompt;

    // ====================================================================
    // getters / setters
    // ====================================================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrimaryStyle() { return primaryStyle; }
    public void setPrimaryStyle(String primaryStyle) { this.primaryStyle = primaryStyle; }

    public String getSecondaryStyle() { return secondaryStyle; }
    public void setSecondaryStyle(String secondaryStyle) { this.secondaryStyle = secondaryStyle; }

    public String getCrowd() { return crowd; }
    public void setCrowd(String crowd) { this.crowd = crowd; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public List<String> getSourceRefs() { return sourceRefs; }
    public void setSourceRefs(List<String> sourceRefs) { this.sourceRefs = sourceRefs; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    @Override
    public String toString() {
        return "StyleCombo{id=" + id + ", primary=" + primaryStyle
                + ", secondary=" + secondaryStyle + ", tier=" + tier + "}";
    }
}

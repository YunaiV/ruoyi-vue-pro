package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * ProductDraftAgent — Phase 8 商品草稿生成（简洁版）。
 *
 * <h3>与 ProductAgent 的区别</h3>
 * <ul>
 *   <li>ProductAgent：调用 CopyService 生成 AI 文案（适合纯关键词驱动流程）</li>
 *   <li>ProductDraftAgent：Phase 8 选款落地，用 style+category 直接生成标题，
 *       并把选定的设计图（{@code ctx.selectedImage}）写入 {@code cdnImageUrl}，
 *       不依赖外部 AI 文案服务</li>
 * </ul>
 *
 * <h3>写入字段</h3>
 * <ul>
 *   <li>{@code chain_code}  — 链码</li>
 *   <li>{@code title}       — "{style}风{category}"</li>
 *   <li>{@code description} — "适合{market}市场，爆款潜力款"</li>
 *   <li>{@code cdn_image_url} — ctx.selectedImage</li>
 *   <li>{@code category}    — ctx.category</li>
 *   <li>{@code status}      — DRAFT</li>
 *   <li>{@code stock}       — 0（由 InventoryAgent 初始化为 10）</li>
 *   <li>{@code sold_count}  — 0</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 每次执行在 deepay_product 新增一条 DRAFT 记录</li>
 *   <li>✔ ctx.productId 写回，供 PricingAgent / PublishAgent 使用</li>
 *   <li>✔ cdnImageUrl 指向 selectedImage（DesignSelectAgent 选出的图）</li>
 *   <li>✔ category / crowd 字段落库，供 TrendAgent 后续过滤</li>
 * </ul>
 */
@Component
public class ProductDraftAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ProductDraftAgent.class);

    @Resource
    private DeepayProductMapper deepayProductMapper;

    @Override
    public Context run(Context ctx) {
        DeepayProductDO product = new DeepayProductDO();

        // 链路 ID
        product.setChainCode(ctx.chainCode);

        // 标题 / 描述
        product.setTitle(buildTitle(ctx));
        product.setDescription(buildDesc(ctx));
        ctx.title       = product.getTitle();
        ctx.description = product.getDescription();

        // 封面图（DesignSelectAgent 选出的设计图）
        if (StringUtils.hasText(ctx.selectedImage)) {
            product.setCdnImageUrl(ctx.selectedImage);
        }

        // 品类（供 TrendAgent 精准过滤）
        if (StringUtils.hasText(ctx.category)) {
            product.setCategory(ctx.category);
        }

        // 初始状态
        product.setStatus("DRAFT");
        product.setStock(0);        // InventoryAgent 初始化为 10
        product.setSoldCount(0);
        product.setCreatedAt(LocalDateTime.now());

        deepayProductMapper.insert(product);

        ctx.productId = String.valueOf(product.getId());
        log.info("[ProductDraftAgent] 草稿已创建 chainCode={} productId={} title={} image={}",
                ctx.chainCode, ctx.productId, product.getTitle(),
                product.getCdnImageUrl() != null ? "✓" : "✗");
        return ctx;
    }

    // ====================================================================
    // 文案生成（Phase 8 简版：style + category）
    // ====================================================================

    private String buildTitle(Context ctx) {
        String style    = StringUtils.hasText(ctx.style)    ? ctx.style    : "";
        String category = StringUtils.hasText(ctx.category) ? ctx.category : (StringUtils.hasText(ctx.keyword) ? ctx.keyword : "新款");
        if (StringUtils.hasText(style)) {
            return style + "风" + category;
        }
        return category;
    }

    private String buildDesc(Context ctx) {
        String market = StringUtils.hasText(ctx.market) ? ctx.market : "CN";
        String crowd  = StringUtils.hasText(ctx.crowd)  ? "，目标客群：" + ctx.crowd : "";
        return "适合" + marketLabel(market) + "市场" + crowd + "，爆款潜力款";
    }

    private String marketLabel(String market) {
        switch (market.toUpperCase()) {
            case "EU": return "欧洲";
            case "US": return "北美";
            case "ME": return "中东";
            default:   return "国内";
        }
    }

}

package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.service.CopyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * ProductAgent — 生成商品信息（title / description），落库 deepay_product（状态 DRAFT）。
 *
 * <p>文案由 {@link CopyService} 生成（调用 AI 文本 API，未配置时降级为模板字符串）。
 * 后续 PricingAgent 写价格，PublishAgent 将状态改为 SELLING。</p>
 */
@Component
public class ProductAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ProductAgent.class);

    @Resource
    private DeepayProductMapper deepayProductMapper;

    @Resource
    private CopyService copyService;

    @Override
    public Context run(Context ctx) {
        String keyword = ctx.keyword != null ? ctx.keyword : "新款";
        ctx.title       = copyService.generateTitle(keyword);
        ctx.description = copyService.generateDescription(keyword);

        DeepayProductDO product = new DeepayProductDO();
        product.setChainCode(ctx.chainCode);
        product.setTitle(ctx.title);
        product.setDescription(ctx.description);
        // 写入封面图（DesignSelectAgent / AIDecisionAgent 选出的图）
        if (ctx.selectedImage != null) {
            product.setCdnImageUrl(ctx.selectedImage);
        }
        // 品类落库（供 TrendAgent 精准过滤）
        if (ctx.category != null) {
            product.setCategory(ctx.category);
        }
        product.setStatus("DRAFT");
        product.setSoldCount(0);
        product.setStock(0);
        product.setCreatedAt(LocalDateTime.now());
        deepayProductMapper.insert(product);

        ctx.productId = String.valueOf(product.getId());
        log.info("ProductAgent: 商品草稿已创建，chainCode={} productId={} title={}", ctx.chainCode, ctx.productId, ctx.title);
        return ctx;
    }

}



package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PublishChannelAgent — Phase 10 多渠道发布。
 *
 * <h3>渠道优先级</h3>
 * <ol>
 *   <li>H5 内部页面（必须，始终开启）</li>
 *   <li>1688 B2B API（占位，需配置 {@code deepay.channel.1688.enabled=true}）</li>
 *   <li>Shopify 独立站（占位，需配置 {@code deepay.channel.shopify.enabled=true}）</li>
 * </ol>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>ctx.productUrl  = "/product/{chainCode}"</li>
 *   <li>ctx.channels    = ["H5", ...]（已发布渠道列表）</li>
 *   <li>ctx.productChannel = 主渠道（首个）</li>
 *   <li>落库 deepay_product.channel</li>
 * </ul>
 */
@Component
public class PublishChannelAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PublishChannelAgent.class);

    @Value("${deepay.channel.1688.enabled:false}")
    private boolean enable1688;

    @Value("${deepay.channel.shopify.enabled:false}")
    private boolean enableShopify;

    @Resource
    private DeepayProductMapper productMapper;

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[PublishChannelAgent] shouldProduce=false，跳过");
            return ctx;
        }

        List<String> channels = new ArrayList<>();

        // 1️⃣ H5（必须）
        channels.add("H5");
        ctx.productUrl = "/product/" + ctx.chainCode;

        // 2️⃣ 1688 API 占位
        if (enable1688) {
            try {
                publish1688(ctx);
                channels.add("1688");
            } catch (Exception e) {
                log.warn("[PublishChannelAgent] 1688 发布失败（不影响主流程）chainCode={}", ctx.chainCode, e);
            }
        }

        // 3️⃣ Shopify 占位
        if (enableShopify) {
            try {
                publishShopify(ctx);
                channels.add("Shopify");
            } catch (Exception e) {
                log.warn("[PublishChannelAgent] Shopify 发布失败（不影响主流程）chainCode={}", ctx.chainCode, e);
            }
        }

        ctx.channels       = channels;
        ctx.productChannel = channels.get(0);   // 主渠道
        ctx.published      = true;

        // 落库 channel 字段
        if (StringUtils.hasText(ctx.productId)) {
            try {
                productMapper.updateChannel(Long.parseLong(ctx.productId), String.join(",", channels));
            } catch (Exception e) {
                log.warn("[PublishChannelAgent] 落库 channel 失败 productId={}", ctx.productId, e);
            }
        }

        log.info("[PublishChannelAgent] 发布完成 channels={} url={} chainCode={}",
                channels, ctx.productUrl, ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------
    // 渠道占位实现（后续接真实 API）
    // ----------------------------------------------------------------

    /**
     * 1688 B2B 发布占位。
     * 接入真实 API 时替换此方法，接口不变。
     */
    private void publish1688(Context ctx) {
        // TODO: 调用 1688 开放平台 API
        // 文档：https://open.1688.com/api/apidoc.htm?spm=a260s.11452220.0.0.17341682aHd60R&id=com.alibaba.product:alibaba.product.add-1
        log.info("[PublishChannelAgent] 1688 API 占位（待接入） chainCode={}", ctx.chainCode);
    }

    /**
     * Shopify 独立站发布占位。
     * 接入真实 API 时替换此方法，接口不变。
     */
    private void publishShopify(Context ctx) {
        // TODO: 调用 Shopify Admin API /admin/api/2024-01/products.json
        log.info("[PublishChannelAgent] Shopify API 占位（待接入） chainCode={}", ctx.chainCode);
    }
}

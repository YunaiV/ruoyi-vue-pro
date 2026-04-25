package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * PublishAgent — 将商品上架（status = SELLING），生成访问链接。
 *
 * <p>前置：ProductAgent 已创建 deepay_product 记录（ctx.productId 非空）。</p>
 * <p>输出：
 * <ul>
 *   <li>{@link Context#published} = true</li>
 *   <li>{@link Context#productUrl} = /product/{chainCode}</li>
 * </ul>
 * 同时将 deepay_style_chain.status 更新为 PUBLISHED。
 * </p>
 */
@Component
public class PublishAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PublishAgent.class);

    @Resource
    private DeepayProductMapper deepayProductMapper;

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        // 更新 deepay_product.status → SELLING
        if (ctx.productId != null) {
            deepayProductMapper.updateStatus(Long.parseLong(ctx.productId), "SELLING");
        }

        // 更新 deepay_style_chain.status → PUBLISHED
        if (ctx.chainCode != null) {
            deepayStyleChainMapper.update(null, new LambdaUpdateWrapper<DeepayStyleChainDO>()
                    .eq(DeepayStyleChainDO::getChainCode, ctx.chainCode)
                    .set(DeepayStyleChainDO::getStatus, "PUBLISHED"));
        }

        ctx.productUrl = "/product/" + ctx.chainCode;
        ctx.published  = true;

        log.info("PublishAgent: 商品已上架，chainCode={} url={}", ctx.chainCode, ctx.productUrl);
        return ctx;
    }

}




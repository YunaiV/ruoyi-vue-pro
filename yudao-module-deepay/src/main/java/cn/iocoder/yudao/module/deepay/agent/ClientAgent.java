package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayClientDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayClientMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * ClientAgent — Phase 5 客户分层系统。
 *
 * <p>根据客户等级调整定价和供货优先级：
 * <pre>
 *   A 类（大客户）：price × 0.85，优先供货
 *   B 类（标准）  ：price × 1.00，正常
 *   C 类（限量）  ：price × 1.05，限量供货
 * </pre>
 * 结果写入 {@link Context#wholesalePrice}，并累加客户历史下单金额。</p>
 */
@Component
public class ClientAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ClientAgent.class);

    private static final BigDecimal DISCOUNT_A = new BigDecimal("0.85");
    private static final BigDecimal DISCOUNT_B = BigDecimal.ONE;
    private static final BigDecimal PREMIUM_C  = new BigDecimal("1.05");

    @Resource
    private DeepayClientMapper clientMapper;

    @Override
    public Context run(Context ctx) {
        if (ctx.clientId == null) {
            log.debug("[ClientAgent] 无 clientId，跳过客户分层");
            return ctx;
        }

        DeepayClientDO client = clientMapper.selectById(ctx.clientId);
        if (client == null) {
            log.warn("[ClientAgent] 客户不存在 clientId={}", ctx.clientId);
            return ctx;
        }

        ctx.clientLevel = client.getLevel();
        BigDecimal basePrice = ctx.price != null ? ctx.price : BigDecimal.ZERO;

        BigDecimal multiplier;
        switch (client.getLevel()) {
            case "A":
                multiplier = DISCOUNT_A;
                break;
            case "C":
                multiplier = PREMIUM_C;
                break;
            default:
                multiplier = DISCOUNT_B;
        }

        ctx.wholesalePrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

        // 计算批发总利润
        if (ctx.costPrice != null && ctx.wholesaleQty != null && ctx.wholesaleQty > 0) {
            BigDecimal unitProfit = ctx.wholesalePrice.subtract(ctx.costPrice);
            ctx.wholesaleProfit = unitProfit.multiply(new BigDecimal(ctx.wholesaleQty))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 累加客户历史下单金额
        if (ctx.wholesalePrice != null && ctx.wholesaleQty != null) {
            BigDecimal orderAmount = ctx.wholesalePrice.multiply(new BigDecimal(ctx.wholesaleQty));
            // 使用参数化更新，避免 SQL 注入
            clientMapper.update(null, new LambdaUpdateWrapper<DeepayClientDO>()
                    .eq(DeepayClientDO::getId, ctx.clientId)
                    .setSql("total_order_amount = total_order_amount + {0}", orderAmount)
                    .set(DeepayClientDO::getUpdatedAt, LocalDateTime.now()));

            // 动态升级：累计超过 10 万自动升为 A
            autoUpgradeLevel(client, orderAmount);
        }

        log.info("[ClientAgent] 客户分层完成 clientId={} level={} basePrice={} wholesalePrice={} qty={}",
                ctx.clientId, ctx.clientLevel, basePrice, ctx.wholesalePrice, ctx.wholesaleQty);
        return ctx;
    }

    private void autoUpgradeLevel(DeepayClientDO client, BigDecimal newOrderAmount) {
        BigDecimal total = (client.getTotalOrderAmount() != null ? client.getTotalOrderAmount() : BigDecimal.ZERO)
                .add(newOrderAmount);
        String newLevel = null;
        if (total.compareTo(new BigDecimal("100000")) >= 0 && !"A".equals(client.getLevel())) {
            newLevel = "A";
        } else if (total.compareTo(new BigDecimal("30000")) >= 0 && "C".equals(client.getLevel())) {
            newLevel = "B";
        }
        if (newLevel != null) {
            clientMapper.update(null, new LambdaUpdateWrapper<DeepayClientDO>()
                    .eq(DeepayClientDO::getId, client.getId())
                    .set(DeepayClientDO::getLevel, newLevel)
                    .set(DeepayClientDO::getUpdatedAt, LocalDateTime.now()));
            log.info("[ClientAgent] 客户自动升级 clientId={} {} → {}", client.getId(), client.getLevel(), newLevel);
        }
    }

}

package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FinanceAgent — 创建订单，生成支付 ID，落库 deepay_order。
 *
 * <p>流程：
 * <ol>
 *   <li>创建 deepay_order 记录，状态 PENDING。</li>
 *   <li>生成唯一 paymentId = "PAY-{chainCode}-{timestamp}"。</li>
 *   <li>写回 {@link Context#orderId}、{@link Context#paymentId}、{@link Context#paid}=false。</li>
 * </ol>
 * 支付完成后，通过 {@code POST /deepay/callback/payment} 回调更新状态。
 * </p>
 */
@Component
public class FinanceAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(FinanceAgent.class);

    @Resource
    private DeepayOrderMapper deepayOrderMapper;

    @Override
    public Context run(Context ctx) {
        String code   = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";
        BigDecimal amount = ctx.price != null ? ctx.price : BigDecimal.ZERO;

        String paymentId = "PAY-" + code + "-" + System.currentTimeMillis();

        DeepayOrderDO order = new DeepayOrderDO();
        order.setPaymentId(paymentId);
        order.setChainCode(code);
        order.setAmount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        deepayOrderMapper.insert(order);

        ctx.orderId   = String.valueOf(order.getId());
        ctx.paymentId = paymentId;
        ctx.paid      = false;
        // 向后兼容 ChainOrchestrator
        ctx.iban      = "DEEPAY-" + code;

        log.info("FinanceAgent: 订单已创建，orderId={} paymentId={} amount={}", ctx.orderId, paymentId, amount);
        return ctx;
    }

}


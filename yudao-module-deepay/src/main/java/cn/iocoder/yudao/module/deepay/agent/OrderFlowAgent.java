package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.service.FxRateService;
import cn.iocoder.yudao.module.deepay.service.JeepayProperties;
import cn.iocoder.yudao.module.deepay.service.PaymentServiceV2;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderFlowAgent — Phase 10 升级版订单流（替代 FinanceAgent 进入主链路）。
 *
 * <h3>多货币设计</h3>
 * <pre>
 * ctx.basePrice   = EUR 基准价（PricingStrategyAgent 设置）
 * ctx.userCurrency = 用户货币（Controller 从 IP 识别后注入）
 *
 * order.baseAmount    = ctx.basePrice          (EUR，分析用)
 * order.displayAmount = convert(basePrice, USD) (展示用)
 * order.currency      = USD
 *
 * Jeepay 支付金额 = displayAmount × 100（分）
 * </pre>
 */
@Component
public class OrderFlowAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(OrderFlowAgent.class);

    @Resource private DeepayOrderMapper  deepayOrderMapper;
    @Resource private PaymentServiceV2   paymentService;
    @Resource private JeepayProperties   jeepayProperties;
    @Resource private FxRateService      fxRateService;

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[OrderFlowAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String     code      = ctx.chainCode  != null ? ctx.chainCode  : "UNKNOWN";
        // baseAmount 永远 EUR；displayAmount 是用户付款货币
        BigDecimal basePrice  = ctx.basePrice  != null ? ctx.basePrice  :
                               (ctx.price      != null ? ctx.price      : BigDecimal.ZERO);
        String     currency   = StringUtils.hasText(ctx.userCurrency) ? ctx.userCurrency : "EUR";

        // 展示金额 = basePrice 换算到用户货币
        BigDecimal displayAmt = fxRateService.convert(basePrice, currency);
        ctx.displayPrice = displayAmt;

        // ── 防重复下单（同 chainCode + userId）──────────────────────
        if (ctx.userId != null) {
            DeepayOrderDO existing = deepayOrderMapper.selectByChainCodeAndUserId(code, ctx.userId);
            if (existing != null && "PENDING".equals(existing.getStatus())) {
                log.info("[OrderFlowAgent] 复用已有 PENDING 订单 orderId={} paymentId={}",
                        existing.getId(), existing.getPaymentId());
                ctx.orderId   = String.valueOf(existing.getId());
                ctx.paymentId = existing.getPaymentId();
                ctx.paid      = false;
                return ctx;
            }
        }

        // ── 通过支付中台创建支付单（使用 displayAmount + currency）──
        String subject    = StringUtils.hasText(ctx.title) ? ctx.title : ("Deepay-" + code);
        // outTradeNo = 我方唯一标识（绑定 paymentId，映射到 Jeepay outTradeNo）
        String outTradeNo = "PAY-" + code + "-" + System.currentTimeMillis();

        // PaymentServiceV2.create 传展示金额 + 用户货币，插件内转换为"分"
        PaymentResp resp = paymentService.create(
                outTradeNo, displayAmt, subject, jeepayProperties.getNotifyUrl(), currency);

        // ── 落库订单 ─────────────────────────────────────────────────
        DeepayOrderDO order = new DeepayOrderDO();
        order.setPaymentId(outTradeNo);          // 我方ID = outTradeNo
        order.setChainCode(code);
        order.setUserId(ctx.userId);
        order.setBaseAmount(basePrice);          // EUR 基准金额（分析用）
        order.setDisplayAmount(displayAmt);      // 用户实付金额（展示用）
        order.setAmount(displayAmt);             // 向后兼容
        order.setCurrency(currency);             // 用户货币
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        deepayOrderMapper.insert(order);

        ctx.orderId      = String.valueOf(order.getId());
        ctx.paymentId    = outTradeNo;
        ctx.paid         = false;
        ctx.iban         = "DEEPAY-" + code;     // 向后兼容
        ctx.userCurrency = currency;

        log.info("[OrderFlowAgent] 订单已创建 orderId={} paymentId={} basePrice={}EUR " +
                        "displayAmt={}{} wayCode=via{} payUrl={}",
                ctx.orderId, outTradeNo, basePrice, displayAmt, currency, currency,
                resp.isSuccess() ? resp.getPayUrl() : "FAILED:" + resp.getErrorMsg());
        return ctx;
    }
}


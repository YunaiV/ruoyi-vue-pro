package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.coinbase;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Coinbase Commerce 支付客户端
 * <p>
 * 对接 <a href="https://docs.cloud.coinbase.com/commerce/reference">Coinbase Commerce REST API v2</a>。
 * <p>
 * 流程：
 * <pre>
 *  unifiedOrder  → POST https://api.commerce.coinbase.com/charges
 *                   返回 hosted_url（URL redirect 展示给用户）
 *  parseOrderNotify → 验证 X-CC-Webhook-Signature，解析 charge:confirmed 等事件
 *  getOrder       → GET  https://api.commerce.coinbase.com/charges/{code}
 * </pre>
 * 退款：Coinbase Commerce 不支持程序化退款，返回不支持提示；
 * 转账：不支持。
 *
 * @author deepay
 */
@Slf4j
public class CoinbaseCommercePayClient extends AbstractPayClient<CoinbaseCommercePayClientConfig> {

    private static final String API_BASE = "https://api.commerce.coinbase.com";
    private static final String CHARGES_PATH = "/charges";
    private static final String API_VERSION_HEADER = "2018-03-22";

    /** 默认人民币兑 USD 汇率（fallback 值，实际应通过 CryptoUsdcPayClientConfig.cnyToUsdcRate 或汇率服务获取）
     *  Coinbase Commerce 计价货币为 USD 时使用；如配置 currency=USD 且订单为人民币，则按此汇率换算。
     *  <b>生产环境请通过 {@code CoinbaseCommercePayClientConfig} 的 {@code cnyToUsdRate} 字段配置实时汇率。</b>
     */
    private static final BigDecimal DEFAULT_CNY_TO_USD_RATE = new BigDecimal("7.20");

    public CoinbaseCommercePayClient(Long channelId, CoinbaseCommercePayClientConfig config) {
        super(channelId, PayChannelEnum.COINBASE_COMMERCE.getCode(), config);
    }

    @Override
    protected void doInit() {
        // HTTP 客户端无需初始化
    }

    // ==================== 支付 ====================

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        // 金额：系统存分，Coinbase 要求 USD 小数字符串
        String priceStr = fenpToDecimalStr(reqDTO.getPrice(), config.getCurrency());

        JSONObject body = JSONUtil.createObj()
                .set("name", reqDTO.getSubject())
                .set("description", reqDTO.getBody() != null ? reqDTO.getBody() : reqDTO.getSubject())
                .set("local_price", JSONUtil.createObj()
                        .set("amount", priceStr)
                        .set("currency", config.getCurrency()))
                .set("pricing_type", "fixed_price")
                .set("metadata", JSONUtil.createObj()
                        .set("merchant_order_id", reqDTO.getMerchantOrderId())
                        .set("out_trade_no", reqDTO.getOutTradeNo()));

        if (config.getRedirectUrl() != null) {
            body.set("redirect_url", config.getRedirectUrl());
        }
        if (config.getCancelUrl() != null) {
            body.set("cancel_url", config.getCancelUrl());
        }

        HttpResponse resp = HttpRequest.post(API_BASE + CHARGES_PATH)
                .header("X-CC-Api-Key", config.getApiKey())
                .header("X-CC-Version", API_VERSION_HEADER)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(10_000)
                .execute();

        if (!resp.isOk()) {
            log.error("[doUnifiedOrder][Coinbase 创建 charge 失败 status={} body={}]", resp.getStatus(), resp.body());
            return PayOrderRespDTO.closedOf(String.valueOf(resp.getStatus()),
                    "Coinbase 创建 charge 失败: " + resp.body(),
                    reqDTO.getOutTradeNo(), resp.body());
        }

        JSONObject data = JSONUtil.parseObj(resp.body()).getJSONObject("data");
        String chargeCode = data.getStr("code");
        String hostedUrl = data.getStr("hosted_url");

        log.info("[doUnifiedOrder][Coinbase charge 创建成功 chargeCode={} outTradeNo={}]",
                chargeCode, reqDTO.getOutTradeNo());

        return PayOrderRespDTO.waitingOf(
                PayOrderDisplayModeEnum.URL.getMode(),
                hostedUrl,
                reqDTO.getOutTradeNo(),
                data);
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers)
            throws Throwable {
        // 1. 验证 Webhook 签名
        String signature = headers.getOrDefault("x-cc-webhook-signature",
                headers.getOrDefault("X-CC-Webhook-Signature", ""));
        if (!verifyWebhookSignature(body, signature)) {
            log.warn("[doParseOrderNotify][Coinbase Webhook 签名验证失败 signature={}]", signature);
            throw new SecurityException("Coinbase Webhook 签名验证失败");
        }

        // 2. 解析事件
        JSONObject event = JSONUtil.parseObj(body).getJSONObject("event");
        String eventType = event.getStr("type");   // e.g. "charge:confirmed"
        JSONObject chargeData = event.getJSONObject("data");

        String chargeCode = chargeData.getStr("code");
        JSONObject metadata = chargeData.getJSONObject("metadata");
        String outTradeNo = metadata != null ? metadata.getStr("out_trade_no") : chargeCode;

        log.info("[doParseOrderNotify][Coinbase 回调 eventType={} chargeCode={} outTradeNo={}]",
                eventType, chargeCode, outTradeNo);

        switch (eventType) {
            case "charge:confirmed":
            case "charge:resolved": {
                LocalDateTime successTime = parseConfirmedTime(chargeData);
                return PayOrderRespDTO.successOf(chargeCode, null, successTime, outTradeNo, chargeData);
            }
            case "charge:failed":
            case "charge:canceled": {
                return PayOrderRespDTO.closedOf("CHARGE_" + eventType.toUpperCase().replace(":", "_"),
                        "Coinbase 支付 " + eventType, outTradeNo, chargeData);
            }
            default: {
                // charge:created / charge:pending — 仍在等待中
                return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(),
                        chargeData.getStr("hosted_url"), outTradeNo, chargeData);
            }
        }
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        // outTradeNo 此处对应 chargeCode（在 metadata 中存储，查询时需要先通过 list 过滤；
        // 为简化实现，直接尝试 GET /charges/{outTradeNo}，若 code 与 outTradeNo 不同则业务侧需传 chargeCode）
        HttpResponse resp = HttpRequest.get(API_BASE + CHARGES_PATH + "/" + outTradeNo)
                .header("X-CC-Api-Key", config.getApiKey())
                .header("X-CC-Version", API_VERSION_HEADER)
                .timeout(10_000)
                .execute();

        if (resp.getStatus() == 404) {
            return PayOrderRespDTO.closedOf("NOT_FOUND", "Coinbase charge 不存在", outTradeNo, null);
        }
        if (!resp.isOk()) {
            log.error("[doGetOrder][Coinbase 查询 charge 失败 status={} body={}]", resp.getStatus(), resp.body());
            return PayOrderRespDTO.closedOf(String.valueOf(resp.getStatus()),
                    "Coinbase 查询失败: " + resp.body(), outTradeNo, resp.body());
        }

        JSONObject data = JSONUtil.parseObj(resp.body()).getJSONObject("data");
        String timeline = latestTimelineStatus(data);

        if ("COMPLETED".equalsIgnoreCase(timeline) || "RESOLVED".equalsIgnoreCase(timeline)) {
            return PayOrderRespDTO.successOf(data.getStr("code"), null,
                    parseConfirmedTime(data), outTradeNo, data);
        }
        if ("CANCELED".equalsIgnoreCase(timeline) || "EXPIRED".equalsIgnoreCase(timeline)) {
            return PayOrderRespDTO.closedOf("CHARGE_" + timeline, "Coinbase charge " + timeline,
                    outTradeNo, data);
        }
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(),
                data.getStr("hosted_url"), outTradeNo, data);
    }

    // ==================== 退款（Coinbase Commerce 不支持程序化退款） ====================

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        return PayRefundRespDTO.failureOf("UNSUPPORTED",
                "Coinbase Commerce 不支持程序化退款，请在 Coinbase 控制台手动处理",
                reqDTO.getOutRefundNo(), null);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers)
            throws Throwable {
        throw new UnsupportedOperationException("Coinbase Commerce 无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Throwable {
        return PayRefundRespDTO.failureOf("UNSUPPORTED",
                "Coinbase Commerce 不支持退款查询", outRefundNo, null);
    }

    // ==================== 转账（不支持） ====================

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) throws Throwable {
        throw new UnsupportedOperationException("Coinbase Commerce 不支持转账");
    }

    @Override
    protected PayTransferRespDTO doParseTransferNotify(Map<String, String> params, String body, Map<String, String> headers)
            throws Throwable {
        throw new UnsupportedOperationException("Coinbase Commerce 不支持转账回调");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo) throws Throwable {
        throw new UnsupportedOperationException("Coinbase Commerce 不支持转账查询");
    }

    // ==================== 工具方法 ====================

    /**
     * 验证 Coinbase Commerce Webhook 签名（HMAC-SHA256）
     */
    private boolean verifyWebhookSignature(String body, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        try {
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256,
                    config.getWebhookSecret().getBytes(StandardCharsets.UTF_8));
            String computed = HexUtil.encodeHexStr(hmac.digest(body));
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("[verifyWebhookSignature][签名计算失败]", e);
            return false;
        }
    }

    /**
     * 分转 USD 字符串（系统用"分"存储，Coinbase 要求小数形式）
     * 若计价货币为 CNY，则换算为 USD。
     */
    private String fenpToDecimalStr(Integer fen, String currency) {
        BigDecimal yuan = BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if ("CNY".equalsIgnoreCase(currency)) {
            // 换算为 USD（使用 config 中的可配置汇率）
            BigDecimal rate = config.getCnyToUsdRate() != null ? config.getCnyToUsdRate() : DEFAULT_CNY_TO_USD_RATE;
            yuan = yuan.divide(rate, 2, RoundingMode.HALF_UP);
        }
        return yuan.toPlainString();
    }

    /**
     * 从 charge data 取最新的 timeline 状态
     */
    private String latestTimelineStatus(JSONObject data) {
        try {
            var timeline = data.getJSONArray("timeline");
            if (timeline != null && !timeline.isEmpty()) {
                return timeline.getJSONObject(timeline.size() - 1).getStr("status");
            }
        } catch (Exception e) {
            log.warn("[latestTimelineStatus][解析 timeline 失败]", e);
        }
        return "NEW";
    }

    /**
     * 尝试从 charge 的 timeline 中解析确认时间
     */
    private LocalDateTime parseConfirmedTime(JSONObject data) {
        try {
            var timeline = data.getJSONArray("timeline");
            if (timeline != null) {
                for (int i = timeline.size() - 1; i >= 0; i--) {
                    JSONObject entry = timeline.getJSONObject(i);
                    String status = entry.getStr("status");
                    if ("COMPLETED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status)) {
                        String timeStr = entry.getStr("time");
                        if (timeStr != null) {
                            return LocalDateTime.parse(timeStr.replace("Z", ""),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[parseConfirmedTime][解析确认时间失败]", e);
        }
        return LocalDateTime.now();
    }

}

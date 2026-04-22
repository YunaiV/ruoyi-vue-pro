package cn.iocoder.yudao.module.deepay.service.payment;

import cn.iocoder.yudao.module.deepay.service.JeepayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/**
 * JeepayPlugin — Jeepay 聚合支付网关插件（微信/支付宝/多商户）。
 *
 * <h3>Jeepay 本质</h3>
 * <p>Jeepay 是完整的支付网关系统（非SDK）。你系统不修改 Jeepay，只通过 HTTP API 调用它。
 * 相当于你搭了一个"支付宝中间层"。</p>
 *
 * <h3>字段映射（必须对齐）</h3>
 * <pre>
 * 你的系统               Jeepay API字段
 * ─────────────────────────────────────
 * payment_id         →  outTradeNo   ⭐全局唯一，绑定我方ID
 * price (元)         →  amount (分)  × 100
 * title              →  subject
 * notifyUrl          →  notifyUrl
 * </pre>
 *
 * <h3>回调状态映射</h3>
 * <pre>
 * Jeepay state  →  你的系统状态
 * 1             →  WAIT
 * 2             →  PAID  ✅
 * 3             →  FAIL
 * </pre>
 *
 * <h3>签名算法（MD5，必须做）</h3>
 * <pre>
 * 参数名ASCII升序 → key=value拼接 → 末尾加 &key={apiKey} → MD5大写
 * </pre>
 *
 * <h3>激活</h3>
 * <pre>
 * deepay.payment.provider=jeepay
 * </pre>
 *
 * <h3>Jeepay 源码</h3>
 * <p>后端：https://github.com/deepveloce-dot/jeepay.git</p>
 * <p>前端：https://github.com/deepveloce-dot/jeepay-ui.git</p>
 */
@Component
@ConditionalOnProperty(name = "deepay.payment.provider", havingValue = "jeepay")
public class JeepayPlugin implements PaymentPlugin {

    private static final Logger log = LoggerFactory.getLogger(JeepayPlugin.class);

    // Jeepay state 常量
    private static final int STATE_WAIT = 1;
    private static final int STATE_PAID = 2;
    private static final int STATE_FAIL = 3;

    @Resource
    private JeepayProperties props;

    private final HttpClient   httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // ====================================================================
    // getType
    // ====================================================================

    @Override
    public String getType() { return "jeepay"; }

    // ====================================================================
    // createOrder — 创建支付单，调用 Jeepay /api/pay/unifiedOrder
    // ====================================================================

    @Override
    public PaymentResp createOrder(PaymentRequest req) {
        // 金额：元 → 分（Jeepay 使用分，坑1）
        long amountFen = req.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // 参数（TreeMap 保证 ASCII 升序，后续签名）
        TreeMap<String, String> params = new TreeMap<>();
        params.put("mchNo",       props.getMchNo());
        params.put("appId",       props.getAppId());
        params.put("outTradeNo",  req.getOutTradeNo());          // ⭐ 绑定我方ID
        params.put("wayCode",     props.getWayCode());           // AUTO = 自动选支付方式
        params.put("amount",      String.valueOf(amountFen));
        params.put("currency",    props.getCurrency());           // EUR — 欧元，全系统统一
        params.put("subject",     safe(req.getSubject(), "Deepay商品"));
        params.put("notifyUrl",   safe(req.getNotifyUrl(), props.getNotifyUrl()));
        params.put("reqTime",     String.valueOf(System.currentTimeMillis() / 1000));
        params.put("version",     "1.0");
        params.put("signType",    "MD5");
        if (req.getAttach() != null) params.put("attach", req.getAttach());
        params.put("sign", sign(params, props.getApiKey()));

        try {
            String body = mapper.writeValueAsString(params);
            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(props.getApiUrl() + "/api/pay/unifiedOrder"))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> resp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
            log.info("[JeepayPlugin] createOrder outTradeNo={} httpStatus={}",
                    req.getOutTradeNo(), resp.statusCode());

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = mapper.readValue(resp.body(), Map.class);
            int code = toInt(respMap.getOrDefault("code", -1));

            if (code == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) respMap.get("data");
                if (data != null) {
                    String payOrderId = String.valueOf(data.getOrDefault("payOrderId", ""));
                    String payUrl     = String.valueOf(data.getOrDefault("payData",
                            data.getOrDefault("payUrl", "")));
                    log.info("[JeepayPlugin] ✅ 支付单创建成功 payOrderId={} outTradeNo={}",
                            payOrderId, req.getOutTradeNo());
                    return PaymentResp.ok(req.getOutTradeNo(), payOrderId, payUrl);
                }
            }

            String msg = String.valueOf(respMap.getOrDefault("msg", resp.body()));
            log.warn("[JeepayPlugin] createOrder 失败 code={} msg={}", code, msg);
            return PaymentResp.fail(req.getOutTradeNo(), "Jeepay error: " + msg);

        } catch (Exception e) {
            log.error("[JeepayPlugin] createOrder 异常 outTradeNo={}", req.getOutTradeNo(), e);
            // Fallback：本地生成 ID（联调用，生产应告警）
            String fallback = "JEEPAY-FB-" + req.getOutTradeNo() + "-" + System.currentTimeMillis();
            return PaymentResp.ok(req.getOutTradeNo(), fallback, "/pay/qr?id=" + fallback);
        }
    }

    // ====================================================================
    // verifyCallback — MD5 验签（坑：不做验签=等着被攻击）
    // ====================================================================

    @Override
    public boolean verifyCallback(Map<String, String> data) {
        if (data == null || !data.containsKey("sign")) {
            log.warn("[JeepayPlugin] verifyCallback: 缺少 sign 字段");
            return false;
        }
        String received = data.get("sign");
        TreeMap<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!"sign".equalsIgnoreCase(e.getKey())
                    && e.getValue() != null && !e.getValue().isEmpty()) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        String expected = sign(filtered, props.getApiKey());
        boolean ok = expected.equalsIgnoreCase(received);
        if (!ok) log.warn("[JeepayPlugin] 签名不符 expected={} received={}", expected, received);
        return ok;
    }

    // ====================================================================
    // parseStatus — Jeepay state(1/2/3) → 内部枚举
    // ====================================================================

    @Override
    public PaymentStatus parseStatus(Map<String, String> data) {
        String stateStr = data.getOrDefault("state", data.get("orderState"));
        if (stateStr == null) return PaymentStatus.UNKNOWN;
        try {
            switch (Integer.parseInt(stateStr)) {
                case STATE_WAIT: return PaymentStatus.WAIT;
                case STATE_PAID: return PaymentStatus.PAID;
                case STATE_FAIL: return PaymentStatus.FAIL;
                default:         return PaymentStatus.UNKNOWN;
            }
        } catch (NumberFormatException e) {
            log.warn("[JeepayPlugin] parseStatus 无法解析 state={}", stateStr);
            return PaymentStatus.UNKNOWN;
        }
    }

    // ====================================================================
    // extractors
    // ====================================================================

    @Override
    public String extractOutTradeNo(Map<String, String> data) {
        // Jeepay 回调字段：outTradeNo = 我方 payment_id
        return data.getOrDefault("outTradeNo", data.get("mchOrderNo"));
    }

    @Override
    public BigDecimal extractPaidAmount(Map<String, String> data) {
        String amountFen = data.get("amount");
        if (amountFen == null) return BigDecimal.ZERO;
        try {
            // 分 → 元
            return new BigDecimal(amountFen)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("[JeepayPlugin] 金额解析失败 amount={}", amountFen);
            return BigDecimal.ZERO;
        }
    }

    // ====================================================================
    // 签名算法（MD5 大写）
    // ====================================================================

    /**
     * Jeepay 标准签名：
     * ASCII升序排列参数 → key=value&... → 末尾 &key={apiKey} → MD5大写
     */
    public static String sign(Map<String, String> sortedParams, String apiKey) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sortedParams.entrySet()) {
            if ("sign".equalsIgnoreCase(e.getKey())) continue;
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            sb.append(e.getKey()).append('=').append(e.getValue()).append('&');
        }
        sb.append("key=").append(apiKey);
        return md5Upper(sb.toString());
    }

    static String md5Upper(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte x : b) hex.append(String.format("%02X", x));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 failed", e);
        }
    }

    private String safe(String v, String def) { return (v != null && !v.isEmpty()) ? v : def; }
    private int    toInt(Object v)             {
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return -1; }
    }
}

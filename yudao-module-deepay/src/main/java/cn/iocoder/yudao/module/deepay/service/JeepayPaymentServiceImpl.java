package cn.iocoder.yudao.module.deepay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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
import java.util.*;

/**
 * JeepayPaymentServiceImpl — Jeepay 聚合支付（微信/支付宝/多商户）。
 *
 * <p>激活条件：application.yml 中 {@code deepay.payment.provider=jeepay}</p>
 *
 * <h3>金额约定</h3>
 * <ul>
 *   <li>入参：元（BigDecimal）</li>
 *   <li>发给 Jeepay：分（×100，取整）</li>
 *   <li>回调中：分（÷100 转元后校验）</li>
 * </ul>
 *
 * <h3>签名算法</h3>
 * <pre>
 * 1. 参数名 ASCII 升序排列，拼接 key=value（跳过空值和 sign 字段）
 * 2. 末尾追加 &amp;key={apiKey}
 * 3. MD5 → 大写
 * </pre>
 *
 * <h3>替换 Stripe / PayPal</h3>
 * <p>新建 {@code StripePaymentServiceImpl} 实现 {@link PaymentService}，
 * 修改 {@code deepay.payment.provider=stripe} 即可，OrderFlowAgent 无需改动。</p>
 */
@Service
@ConditionalOnProperty(name = "deepay.payment.provider", havingValue = "jeepay")
public class JeepayPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(JeepayPaymentServiceImpl.class);

    @Resource
    private JeepayProperties jeepayProperties;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ====================================================================
    // createPayment
    // ====================================================================

    @Override
    public String createPayment(String outTradeNo, BigDecimal amount,
                                String subject, String notifyUrl) {
        // 金额：元 → 分
        long amountFen = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        Map<String, String> params = new TreeMap<>();
        params.put("mchNo",      jeepayProperties.getMchNo());
        params.put("appId",      jeepayProperties.getAppId());
        params.put("outTradeNo", outTradeNo);
        params.put("wayCode",    jeepayProperties.getWayCode());  // WX_JSAPI / ALI_PC / etc.
        params.put("amount",     String.valueOf(amountFen));
        params.put("currency",   "cny");
        params.put("subject",    subject != null ? subject : "Deepay商品");
        params.put("notifyUrl",  notifyUrl);
        params.put("reqTime",    String.valueOf(System.currentTimeMillis() / 1000));
        params.put("version",    "1.0");
        params.put("signType",   "MD5");
        params.put("sign",       sign(params, jeepayProperties.getApiKey()));

        try {
            String body = objectMapper.writeValueAsString(params);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(jeepayProperties.getApiUrl() + "/api/pay/unifiedOrder"))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            log.info("[Jeepay] createPayment outTradeNo={} status={}", outTradeNo, resp.statusCode());

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = objectMapper.readValue(resp.body(), Map.class);
            Object code = respMap.get("code");
            if (code != null && Integer.parseInt(code.toString()) == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) respMap.get("data");
                if (data != null) {
                    Object payOrderId = data.get("payOrderId");
                    if (payOrderId != null) return payOrderId.toString();
                }
            }
            log.warn("[Jeepay] createPayment 失败 resp={}", resp.body());
        } catch (Exception e) {
            log.error("[Jeepay] createPayment 异常 outTradeNo={}", outTradeNo, e);
        }

        // fallback: 本地生成 paymentId（支付宝/微信实际回调不会触发，用于开发联调）
        return "JEEPAY-" + outTradeNo + "-" + System.currentTimeMillis();
    }

    // ====================================================================
    // verifyCallback
    // ====================================================================

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        if (params == null || params.isEmpty()) return false;
        String receivedSign = params.get("sign");
        if (receivedSign == null) return false;

        // 重新计算签名（排除 sign 字段）
        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!"sign".equalsIgnoreCase(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty()) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        String expected = sign(filtered, jeepayProperties.getApiKey());
        boolean ok = expected.equalsIgnoreCase(receivedSign);
        if (!ok) {
            log.warn("[Jeepay] 签名验证失败 expected={} received={}", expected, receivedSign);
        }
        return ok;
    }

    // ====================================================================
    // extractors
    // ====================================================================

    @Override
    public String extractOutTradeNo(Map<String, String> params) {
        // Jeepay 回调字段：outTradeNo
        return params.getOrDefault("outTradeNo", params.get("mchOrderNo"));
    }

    @Override
    public BigDecimal extractPaidAmount(Map<String, String> params) {
        String amountFen = params.get("amount");
        if (amountFen == null) return BigDecimal.ZERO;
        try {
            // 分 → 元
            return new BigDecimal(amountFen)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("[Jeepay] 金额解析失败 amount={}", amountFen);
            return BigDecimal.ZERO;
        }
    }

    // ====================================================================
    // Sign
    // ====================================================================

    /**
     * Jeepay 签名：参数名 ASCII 升序，key=value 拼接，末尾 &key={apiKey}，MD5 大写。
     */
    static String sign(Map<String, String> sortedParams, String apiKey) {
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
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02X", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 failed", e);
        }
    }
}

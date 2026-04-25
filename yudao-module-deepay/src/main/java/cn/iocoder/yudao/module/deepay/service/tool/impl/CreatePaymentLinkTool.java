package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工具：生成收款链接（草稿，MEDIUM 风险）。
 */
@Slf4j
@Component
public class CreatePaymentLinkTool implements AiTool {

    @Override public String getName() { return "createPaymentLink"; }

    @Override
    public String getDescription() {
        return "为指定订单生成收款链接（草稿模式，不立即扣款，需在管理端确认后生效）。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("orderId",  f("integer", "订单ID（必填）"));
        props.put("amount",   f("number",  "收款金额（EUR）"));
        props.put("currency", f("string",  "货币，默认 EUR"));
        schema.put("properties", props);
        schema.put("required", new String[]{"orderId", "amount"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        Object orderIdObj = params.get("orderId");
        Object amountObj  = params.get("amount");
        if (orderIdObj == null || amountObj == null) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 orderId 或 amount");
        }
        // 生成草稿收款链接（实际接入 Jeepay 时替换）
        String linkToken = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String paymentLink = "https://pay.deepay.link/" + linkToken;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId",     orderIdObj);
        result.put("amount",      amountObj);
        result.put("currency",    params.getOrDefault("currency", "EUR"));
        result.put("paymentLink", paymentLink);
        result.put("status",      "DRAFT");
        log.info("[CreatePaymentLink] orderId={} link={}", orderIdObj, paymentLink);
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
}

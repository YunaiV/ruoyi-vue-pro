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
 * 工具：生成报价单草稿（MEDIUM 风险）。
 */
@Slf4j
@Component
public class GenerateQuoteDraftTool implements AiTool {

    @Override public String getName() { return "generateQuoteDraft"; }
    @Override
    public String getDescription() {
        return "根据客户需求生成一份报价单草稿（含商品/数量/单价/折扣），不会直接发送，需人工审阅后发出。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("customerId",  f("integer", "客户ID"));
        props.put("productName", f("string",  "商品名称"));
        props.put("quantity",    f("integer", "数量"));
        props.put("unitPrice",   f("number",  "单价（EUR）"));
        props.put("discount",    f("number",  "折扣（0~1，默认1）"));
        props.put("remarks",     f("string",  "备注"));
        schema.put("properties", props);
        schema.put("required", new String[]{"productName", "quantity", "unitPrice"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        String productName = s(params, "productName");
        if (productName == null || productName.isEmpty()) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 productName");
        }
        double qty       = toDouble(params, "quantity", 1);
        double unitPrice = toDouble(params, "unitPrice", 0);
        double discount  = toDouble(params, "discount", 1.0);
        double total     = qty * unitPrice * discount;
        String quoteId   = "QUOTE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("quoteId",     quoteId);
        result.put("customerId",  params.get("customerId"));
        result.put("productName", productName);
        result.put("quantity",    qty);
        result.put("unitPrice",   unitPrice);
        result.put("discount",    discount);
        result.put("totalAmount", total);
        result.put("currency",    "EUR");
        result.put("remarks",     s(params, "remarks"));
        result.put("status",      "DRAFT");
        log.info("[GenerateQuoteDraft] quoteId={} total={}", quoteId, total);
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
    private static String s(Map<String, Object> p, String k) {
        Object v = p.get(k); return v == null ? null : v.toString();
    }
    private static double toDouble(Map<String, Object> p, String k, double def) {
        Object v = p.get(k); if (v == null) return def;
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return def; }
    }
}

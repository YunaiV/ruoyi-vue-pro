package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：创建订单草稿（MEDIUM 风险，写操作）。
 */
@Slf4j
@Component
public class CreateOrderDraftTool implements AiTool {

    @Resource
    private DeepayOrderMapper orderMapper;

    @Override public String getName() { return "createOrderDraft"; }

    @Override
    public String getDescription() {
        return "根据商品和用户信息创建一条草稿订单（status=DRAFT），不会实际扣款，需人工确认后才提交。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("chainCode", field("string",  "商品链码（必填）"));
        props.put("amount",    field("number",  "金额（EUR，必填）"));
        props.put("currency",  field("string",  "货币代码，默认 EUR"));
        schema.put("properties", props);
        schema.put("required", new String[]{"chainCode", "amount"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        String chainCode = getString(params, "chainCode");
        if (chainCode == null || chainCode.isEmpty()) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 chainCode");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(params.get("amount").toString());
        } catch (Exception e) {
            throw new ToolExecutionException(getName(), "INVALID_PARAM", "amount 格式错误");
        }
        String currency = getString(params, "currency");
        if (currency == null) currency = "EUR";

        DeepayOrderDO order = new DeepayOrderDO();
        order.setChainCode(chainCode);
        order.setUserId(ctx.getCustomerId());
        order.setAmount(amount);
        order.setBaseAmount(amount);
        order.setDisplayAmount(amount);
        order.setCurrency(currency);
        order.setStatus("DRAFT");
        orderMapper.insert(order);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId",   order.getId());
        result.put("chainCode", order.getChainCode());
        result.put("amount",    order.getAmount());
        result.put("currency",  order.getCurrency());
        result.put("status",    "DRAFT");
        log.info("[CreateOrderDraft] 草稿订单已创建 orderId={}", order.getId());
        return result;
    }

    private static Map<String, Object> field(String type, String desc) {
        Map<String, Object> f = new LinkedHashMap<>(); f.put("type", type); f.put("description", desc); return f;
    }
    private static String getString(Map<String, Object> p, String key) {
        Object v = p.get(key); return v == null ? null : v.toString();
    }

}

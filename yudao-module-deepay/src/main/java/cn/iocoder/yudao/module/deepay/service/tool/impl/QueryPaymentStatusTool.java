package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：查询支付状态（LOW 风险）。
 */
@Component
public class QueryPaymentStatusTool implements AiTool {

    @Resource
    private DeepayOrderMapper orderMapper;

    @Override public String getName() { return "queryPaymentStatus"; }
    @Override
    public String getDescription() {
        return "按订单ID查询当前支付状态（PENDING/PAID/CANCELLED）及支付单号。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("orderId", f("integer", "订单ID（必填）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"orderId"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.LOW; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        Object idObj = params.get("orderId");
        if (idObj == null) throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 orderId");
        Long orderId;
        try { orderId = Long.parseLong(idObj.toString()); }
        catch (Exception e) { throw new ToolExecutionException(getName(), "INVALID_PARAM", "orderId 格式错误"); }
        DeepayOrderDO order = orderMapper.selectById(orderId);
        if (order == null) throw new ToolExecutionException(getName(), "NOT_FOUND", "订单不存在: " + orderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId",   orderId);
        result.put("status",    order.getStatus());
        result.put("paymentId", order.getPaymentId());
        result.put("amount",    order.getAmount());
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
}

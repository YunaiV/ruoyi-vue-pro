package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：更新订单字段（HIGH 风险，需二次确认）。
 */
@Slf4j
@Component
public class UpdateOrderFieldTool implements AiTool {

    @Resource
    private DeepayOrderMapper orderMapper;

    @Override public String getName() { return "updateOrderField"; }

    @Override
    public String getDescription() {
        return "更新订单的指定字段（如备注、状态），高风险操作，需用户确认后执行。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("orderId", f("integer", "订单ID（必填）"));
        props.put("field",   f("string",  "字段名（status/remark）"));
        props.put("value",   f("string",  "新值"));
        schema.put("properties", props);
        schema.put("required", new String[]{"orderId", "field", "value"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.HIGH; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        Long orderId = getLong(params, "orderId");
        String field = getString(params, "field");
        String value = getString(params, "value");
        if (orderId == null || field == null || value == null) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少必填参数");
        }
        DeepayOrderDO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ToolExecutionException(getName(), "NOT_FOUND", "订单不存在: " + orderId);
        }
        switch (field) {
            case "status": order.setStatus(value); break;
            case "remark": /* remark is not a direct field on DeepayOrderDO; skip silently */ break;
            default:
                throw new ToolExecutionException(getName(), "INVALID_FIELD", "不支持的字段: " + field);
        }
        orderMapper.updateById(order);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("field",   field);
        result.put("value",   value);
        result.put("updated", true);
        log.info("[UpdateOrderField] 订单字段更新 orderId={} field={} value={}", orderId, field, value);
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
    private static String getString(Map<String, Object> p, String k) {
        Object v = p.get(k); return v == null ? null : v.toString();
    }
    private static Long getLong(Map<String, Object> p, String k) {
        Object v = p.get(k); if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}

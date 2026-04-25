package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayClientDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayClientMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：为客户添加备注（LOW 风险）。
 */
@Slf4j
@Component
public class AddCustomerNoteTool implements AiTool {

    @Resource
    private DeepayClientMapper clientMapper;

    @Override public String getName() { return "addCustomerNote"; }
    @Override
    public String getDescription() {
        return "向指定客户的备注字段追加一条记录（不覆盖原有内容，追加到末尾）。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("customerId", f("integer", "客户ID（必填）"));
        props.put("note",       f("string",  "要追加的备注内容（必填）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"customerId", "note"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.LOW; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        Object idObj = params.get("customerId");
        String note  = s(params, "note");
        if (idObj == null || note == null || note.isEmpty()) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 customerId 或 note");
        }
        Long customerId;
        try { customerId = Long.parseLong(idObj.toString()); }
        catch (Exception e) { throw new ToolExecutionException(getName(), "INVALID_PARAM", "customerId 格式错误"); }
        DeepayClientDO client = clientMapper.selectById(customerId);
        if (client == null) throw new ToolExecutionException(getName(), "NOT_FOUND", "客户不存在: " + customerId);
        String existing = client.getRemark() == null ? "" : client.getRemark();
        client.setRemark(existing + (existing.isEmpty() ? "" : "\n") + "[" + LocalDateTime.now().toLocalDate() + "] " + note);
        client.setUpdatedAt(LocalDateTime.now());
        clientMapper.updateById(client);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customerId", customerId);
        result.put("appended",   true);
        log.info("[AddCustomerNote] 备注追加 customerId={}", customerId);
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
    private static String s(Map<String, Object> p, String k) {
        Object v = p.get(k); return v == null ? null : v.toString();
    }
}

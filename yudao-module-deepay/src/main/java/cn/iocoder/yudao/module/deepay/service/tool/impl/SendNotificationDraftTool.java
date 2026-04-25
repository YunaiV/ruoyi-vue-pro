package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工具：发送通知草稿（MEDIUM 风险，草稿模式，不立即发送）。
 */
@Slf4j
@Component
public class SendNotificationDraftTool implements AiTool {

    @Override public String getName() { return "sendNotificationDraft"; }
    @Override
    public String getDescription() {
        return "生成一条通知草稿（站内信/邮件/短信），不立即发送，需在管理端确认后发出。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("targetId",  f("integer", "接收方 ID（客户或用户 ID）"));
        props.put("channel",   f("string",  "发送渠道：sms/email/inner"));
        props.put("subject",   f("string",  "通知标题"));
        props.put("body",      f("string",  "通知正文（必填）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"body"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        String body = s(params, "body");
        if (body == null || body.isEmpty()) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 body");
        }
        String notifId = "NOTIF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notifId",  notifId);
        result.put("targetId", params.get("targetId"));
        result.put("channel",  params.getOrDefault("channel", "inner"));
        result.put("subject",  s(params, "subject"));
        result.put("body",     body);
        result.put("status",   "DRAFT");
        result.put("createdAt", Instant.now().toString());
        log.info("[SendNotificationDraft] 通知草稿已创建 notifId={}", notifId);
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
    private static String s(Map<String, Object> p, String k) {
        Object v = p.get(k); return v == null ? null : v.toString();
    }
}

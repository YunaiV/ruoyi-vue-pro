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
 * 工具：创建客户（MEDIUM 风险）。
 */
@Slf4j
@Component
public class CreateCustomerTool implements AiTool {

    @Resource
    private DeepayClientMapper clientMapper;

    @Override public String getName() { return "createCustomer"; }
    @Override
    public String getDescription() {
        return "创建一个新客户档案，记录客户名称、联系方式和等级（A/B/C）。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name",    f("string", "客户名称（必填）"));
        props.put("contact", f("string", "联系方式（电话/邮箱）"));
        props.put("level",   f("string", "客户等级 A/B/C，默认 B"));
        props.put("remark",  f("string", "备注"));
        schema.put("properties", props);
        schema.put("required", new String[]{"name"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        String name = s(params, "name");
        if (name == null || name.isEmpty()) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 name");
        }
        DeepayClientDO client = new DeepayClientDO();
        client.setName(name);
        client.setContact(s(params, "contact"));
        client.setLevel(params.getOrDefault("level", "B").toString());
        client.setRemark(s(params, "remark"));
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        clientMapper.insert(client);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customerId", client.getId());
        result.put("name",       client.getName());
        result.put("level",      client.getLevel());
        log.info("[CreateCustomer] 客户已创建 id={} name={}", client.getId(), client.getName());
        return result;
    }

    private static Map<String, Object> f(String t, String d) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("type", t); m.put("description", d); return m;
    }
    private static String s(Map<String, Object> p, String k) {
        Object v = p.get(k); return v == null ? null : v.toString();
    }
}

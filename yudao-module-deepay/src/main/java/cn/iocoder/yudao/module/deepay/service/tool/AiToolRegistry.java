package cn.iocoder.yudao.module.deepay.service.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * AI 工具注册中心。
 *
 * <p>所有实现了 {@link AiTool} 接口的 Spring Bean 会自动注入并注册。</p>
 */
@Slf4j
@Service
public class AiToolRegistry {

    /** key = toolName, value = tool instance */
    private final Map<String, AiTool> tools = new LinkedHashMap<>();

    @Resource
    private List<AiTool> allTools;

    @PostConstruct
    public void init() {
        if (allTools != null) {
            for (AiTool tool : allTools) {
                tools.put(tool.getName(), tool);
                log.info("[AiToolRegistry] 注册工具: {} ({})", tool.getName(), tool.getRiskLevel());
            }
        }
        log.info("[AiToolRegistry] 共注册 {} 个工具", tools.size());
    }

    /** 获取工具，不存在时返回 null */
    public AiTool getTool(String name) {
        return tools.get(name);
    }

    /** 列出所有工具定义（供 LLM function calling 构建 schema） */
    public List<Map<String, Object>> listToolSchemas() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiTool tool : tools.values()) {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("name",        tool.getName());
            schema.put("description", tool.getDescription());
            schema.put("parameters",  tool.getParamsSchema());
            schema.put("riskLevel",   tool.getRiskLevel().name());
            result.add(schema);
        }
        return result;
    }

    /** 获取所有工具（不可变视图） */
    public Collection<AiTool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

}

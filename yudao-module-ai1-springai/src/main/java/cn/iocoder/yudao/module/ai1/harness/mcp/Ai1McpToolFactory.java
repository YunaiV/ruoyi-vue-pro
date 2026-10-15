package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.service.mcp.Ai1McpService;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * AI1 MCP 工具工厂：把 Agent 绑定的 MCP 服务暴露的工具，桥接为 Spring AI ToolCallback
 *
 * 1. 工具名：MCP 名称净化 + "__" + 原始工具名；无有效名称时使用 MCP 编号，超长时保留摘要
 * 2. 入参：透传 MCP 工具的 JSON Schema，调用落地仍走 {@link Ai1McpClientTool}
 *
 * 工具挂到 ChatClient 后，由 Spring AI 的工具调用机制统一驱动；与 {@link Ai1McpClientTool} 同包
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1McpToolFactory {

    /**
     * MCP 名称与工具名称的分隔符
     */
    private static final String NAME_SEPARATOR = "__";

    @Resource
    private Ai1McpService mcpService;

    @Resource
    private Ai1McpClientTool mcpClientTool;

    /**
     * 构建 Agent 绑定的 MCP 工具集合；单个服务失败时跳过，不影响会话
     *
     * tools/list 结果已由 {@link Ai1McpClientTool} 按配置指纹缓存，这里只做 ToolCallback 包装
     *
     * @param agent Agent
     * @return 工具集合
     */
    public List<ToolCallback> buildTools(Ai1AgentDO agent) {
        List<ToolCallback> tools = new ArrayList<>();
        if (CollUtil.isEmpty(agent.getMcpIds())) {
            return tools;
        }
        for (Ai1McpDO mcp : mcpService.getMcpList(agent.getMcpIds())) {
            if (CommonStatusEnum.isDisable(mcp.getStatus())) {
                continue;
            }
            tools.addAll(convertList(listTools(agent, mcp), tool -> buildToolCallback(mcp, tool)));
        }
        return tools;
    }

    /**
     * 列举 MCP 服务暴露的工具；失败时记录日志并返回空列表
     *
     * @param agent Agent
     * @param mcp   MCP 服务
     * @return 工具列表
     */
    private List<McpSchema.Tool> listTools(Ai1AgentDO agent, Ai1McpDO mcp) {
        try {
            return mcpClientTool.listTools(mcp);
        } catch (Exception e) {
            log.warn("[listTools][Agent({}) 加载 MCP({}) 工具失败]", agent.getId(), mcp.getName(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建单个工具回调：名称去冲突 + 入参 Schema 透传 + MCP 客户端调用落地
     */
    private ToolCallback buildToolCallback(Ai1McpDO mcp, McpSchema.Tool tool) {
        Function<Map<String, Object>, String> function = arguments ->
                mcpClientTool.callTool(mcp, tool.name(), arguments);
        Map<String, Object> inputSchema = tool.inputSchema();
        if (MapUtil.isEmpty(inputSchema)) {
            inputSchema = new HashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("properties", new HashMap<>());
        }
        return FunctionToolCallback.builder(buildToolName(mcp, tool), function)
                .description(StrUtil.nullToEmpty(tool.description()))
                .inputType(Map.class)
                .inputSchema(JsonUtils.toJsonString(inputSchema))
                .build();
    }

    /**
     * 构建工具名：无有效 MCP 名称时使用编号；超过 64 字符时截短并追加摘要，避免截断后重名
     *
     * @param mcp  MCP 服务
     * @param tool MCP 工具
     * @return 工具名
     */
    private static String buildToolName(Ai1McpDO mcp, McpSchema.Tool tool) {
        String prefix = slugify(mcp.getName());
        if (StrUtil.isEmpty(prefix)) {
            prefix = "mcp_" + mcp.getId();
        }
        String name = prefix + NAME_SEPARATOR + tool.name();
        // OpenAI 兼容接口的工具名最多 64 字符，保留 47 字符前缀 + "_" + 16 位摘要。
        // 摘要由 MCP 编号和原始工具名生成，避免不同服务或工具截断后重名。
        if (name.length() > 64) {
            String suffix = DigestUtil.sha256Hex(mcp.getId() + ":" + tool.name()).substring(0, 16);
            name = name.substring(0, 47) + "_" + suffix;
        }
        return name;
    }

    /**
     * 名称净化：只保留小写字母、数字、下划线（工具名需匹配 ^[a-zA-Z0-9_-]+$）
     */
    static String slugify(String name) {
        if (StrUtil.isBlank(name)) {
            return "";
        }
        String slug = ReUtil.replaceAll(name.toLowerCase(Locale.ROOT), "[^a-z0-9_]+", "_");
        slug = ReUtil.replaceAll(slug, "_+", "_");
        return ReUtil.replaceAll(slug, "^_+|_+$", "");
    }

}

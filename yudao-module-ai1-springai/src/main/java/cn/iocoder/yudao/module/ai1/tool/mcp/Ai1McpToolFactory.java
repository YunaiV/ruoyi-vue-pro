package cn.iocoder.yudao.module.ai1.tool.mcp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.service.mcp.Ai1McpService;
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
 * 1. 工具名：MCP 名称净化 + "__" + 原始工具名，避免多个服务的同名工具冲突
 * 2. 入参：透传 MCP 工具的 JSON Schema，调用落地仍走 {@link Ai1McpClientTool}
 *
 * 工具挂到 ChatClient 后，由 Spring AI 的工具调用机制统一驱动；与 {@link Ai1McpClientTool} 同包，保持 tool 层内依赖
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
     * 构建 Agent 绑定的 MCP 工具集合；单个服务失败时跳过，不影响对话
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
            tools.addAll(convertList(listTools(agent, mcp), toolInfo -> buildToolCallback(mcp, toolInfo)));
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
    private List<Ai1McpClientTool.McpToolInfo> listTools(Ai1AgentDO agent, Ai1McpDO mcp) {
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
    private ToolCallback buildToolCallback(Ai1McpDO mcp, Ai1McpClientTool.McpToolInfo toolInfo) {
        Function<Map<String, Object>, String> function = arguments ->
                mcpClientTool.callTool(mcp, toolInfo.getName(), arguments);
        Map<String, Object> inputSchema = toolInfo.getInputSchema();
        if (MapUtil.isEmpty(inputSchema)) {
            inputSchema = new HashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("properties", new HashMap<>());
        }
        return FunctionToolCallback.builder(buildToolName(mcp, toolInfo), function)
                .description(StrUtil.nullToEmpty(toolInfo.getDescription()))
                .inputType(Map.class)
                .inputSchema(JsonUtils.toJsonString(inputSchema))
                .build();
    }

    /**
     * 构建工具名：MCP 名称净化 + "__" + 原始工具名，避免多个服务的同名工具冲突
     *
     * @param mcp      MCP 服务
     * @param toolInfo 工具信息
     * @return 工具名
     */
    private static String buildToolName(Ai1McpDO mcp, Ai1McpClientTool.McpToolInfo toolInfo) {
        return slugify(mcp.getName()) + NAME_SEPARATOR + toolInfo.getName();
    }

    /**
     * 名称净化：只保留小写字母、数字、下划线（工具名需匹配 ^[a-zA-Z0-9_-]+$）
     */
    static String slugify(String name) {
        if (StrUtil.isEmpty(name)) {
            return "mcp";
        }
        return ReUtil.replaceAll(name.toLowerCase(Locale.ROOT), "[^a-z0-9_]", "_");
    }

}

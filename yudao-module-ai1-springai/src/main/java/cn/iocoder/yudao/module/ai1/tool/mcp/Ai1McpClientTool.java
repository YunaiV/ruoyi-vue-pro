package cn.iocoder.yudao.module.ai1.tool.mcp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.framework.ai.core.config.Ai1ConfigPlaceholders;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO @AI：jdk8 兼容噢；
/**
 * AI1 MCP 客户端工具（官方 Java MCP SDK）：连接、列举工具、调用工具、连通测试
 *
 * 1. 传输层：http（Streamable HTTP）/ stdio（本地进程）；传输方式以 {@link Ai1McpDO#getTransport()} 列为准，
 *    其余参数取自 config JSON（缺失时回退平铺列）
 * 2. 客户端与工具列表按 MCP 编号 + 配置指纹缓存，配置变化时重建并关闭旧连接
 *
 * 注意：stdio 会在服务器上启动本地进程，与源工程行为一致，多租户不可信部署需自行评估
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1McpClientTool {

    /**
     * 远程服务默认的消息端点
     */
    private static final String DEFAULT_HTTP_ENDPOINT = "/mcp";

    @Resource
    private Environment environment;

    // TODO @AI：guava 优化下？
    /**
     * 客户端缓存：MCP 编号 → 客户端
     */
    private final Map<Long, CachedClient> clientCache = new ConcurrentHashMap<>();
    /**
     * 工具列表缓存：MCP 编号 → 工具列表
     */
    private final Map<Long, CachedTools> toolsCache = new ConcurrentHashMap<>();

    /**
     * 获取 MCP 服务暴露的工具列表（带缓存，配置变化自动重建）
     *
     * @param mcp MCP 服务
     * @return 工具列表；列举失败时返回空列表
     */
    // TODO @AI：方法内注释；有点长，需要看懂一点；
    public List<McpToolInfo> listTools(Ai1McpDO mcp) {
        long fingerprint = buildFingerprint(mcp);
        CachedTools cached = toolsCache.get(mcp.getId());
        if (cached != null && cached.fingerprint() == fingerprint) {
            return cached.tools();
        }
        List<McpToolInfo> tools = new ArrayList<>();
        try {
            McpSchema.ListToolsResult result = getOrCreateClient(mcp).listTools();
            if (result != null && result.tools() != null) {
                for (McpSchema.Tool tool : result.tools()) {
                    tools.add(new McpToolInfo(tool.name(), tool.title(), tool.description(), tool.inputSchema()));
                }
            }
        } catch (Exception e) {
            log.warn("[listTools][MCP({}/{}) tools/list 失败]", mcp.getId(), mcp.getName(), e);
        }
        toolsCache.put(mcp.getId(), new CachedTools(fingerprint, tools));
        return tools;
    }

    /**
     * 调用 MCP 工具（阻塞），返回文本结果
     *
     * @param mcp       MCP 服务
     * @param toolName  工具名称（MCP 服务侧的原始名称）
     * @param arguments 工具参数
     * @return 文本结果
     */
    public String callTool(Ai1McpDO mcp, String toolName, Map<String, Object> arguments) {
        McpSyncClient client = getOrCreateClient(mcp);
        // 同一客户端的调用串行执行，避免 stdio 管道并发读写错乱
        synchronized (client) {
            // TODO @AI：“CallToolRequest”过期了，替换下；
            McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(toolName,
                    arguments != null ? arguments : new HashMap<>()));
            return readText(result);
        }
    }

    /**
     * 连通测试：initialize + tools/list，使用临时客户端，不影响缓存
     *
     * @param mcp MCP 服务
     * @return 测试结果
     */
    public McpConnectResult test(Ai1McpDO mcp) {
        long startTime = System.currentTimeMillis();
        McpSyncClient client = null;
        try {
            // 1. 建立连接
            McpClientTransport transport = buildTransport(mcp);
            if (transport == null) {
                return McpConnectResult.fail("配置不完整（缺少必要参数）", startTime);
            }
            client = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(30))
                    .initializationTimeout(Duration.ofSeconds(15))
                    .build();
            client.initialize();

            // 2. 读取服务信息与工具列表
            McpConnectResult result = new McpConnectResult().setConnectable(true).setTools(new ArrayList<>());
            McpSchema.Implementation serverInfo = client.getServerInfo();
            if (serverInfo != null) {
                result.setServerName(serverInfo.name()).setServerVersion(serverInfo.version());
            }
            result.setInstructions(client.getServerInstructions());
            McpSchema.ListToolsResult listResult = client.listTools();
            if (listResult != null && listResult.tools() != null) {
                for (McpSchema.Tool tool : listResult.tools()) {
                    result.getTools().add(new McpToolInfo(tool.name(), tool.title(), tool.description(), null));
                }
            }
            return result.setToolCount(result.getTools().size())
                    .setMessage("连接成功，发现 " + result.getTools().size() + " 个工具")
                    .setElapsedMs(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.warn("[test][MCP({}/{}) 连通测试失败]", mcp.getId(), mcp.getName(), e);
            return McpConnectResult.fail(String.valueOf(e.getMessage()), startTime);
        } finally {
            closeQuietly(client);
        }
    }

    /**
     * 移除并关闭指定 MCP 的客户端（删除、配置变更时调用，释放子进程与连接线程）
     *
     * @param mcpId MCP 编号
     */
    public void evict(Long mcpId) {
        toolsCache.remove(mcpId);
        CachedClient cached = clientCache.remove(mcpId);
        if (cached != null) {
            closeQuietly(cached.client());
        }
    }

    @PreDestroy
    public void destroy() {
        new ArrayList<>(clientCache.keySet()).forEach(this::evict);
    }

    // ==================== 连接 / 传输 ====================

    /**
     * 获取（或构建）MCP 客户端：配置指纹变化时重建，并关闭旧连接
     */
    private McpSyncClient getOrCreateClient(Ai1McpDO mcp) {
        long fingerprint = buildFingerprint(mcp);
        CachedClient cached = clientCache.get(mcp.getId());
        if (cached != null && cached.fingerprint() == fingerprint) {
            return cached.client();
        }
        synchronized (clientCache) {
            cached = clientCache.get(mcp.getId());
            if (cached != null && cached.fingerprint() == fingerprint) {
                return cached.client();
            }
            McpClientTransport transport = buildTransport(mcp);
            if (transport == null) {
                throw new IllegalStateException("MCP(" + mcp.getName() + ") 配置不完整（缺少必要参数）");
            }
            McpSyncClient client = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(60))
                    .initializationTimeout(Duration.ofSeconds(15))
                    .build();
            client.initialize();
            if (cached != null) {
                closeQuietly(cached.client());
            }
            clientCache.put(mcp.getId(), new CachedClient(fingerprint, client));
            return client;
        }
    }

    /**
     * 构建传输层：stdio 走本地进程，其余走 Streamable HTTP；参数缺失时返回 null
     */
    private McpClientTransport buildTransport(Ai1McpDO mcp) {
        McpConfig config = resolveConfig(parseConfig(mcp));
        try {
            return Ai1McpTransportEnum.isStdio(config.getTransport()) ? buildStdioTransport(config) : buildHttpTransport(config);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception e) {
            log.warn("[buildTransport][MCP({}) 传输层构建失败]", mcp.getId(), e);
            return null;
        }
    }

    /**
     * Streamable HTTP 传输：按「base = scheme://host:port、endpoint = url.path」构建，
     * 保证配置的服务地址精确作为 MCP 消息端点（避免 resolve("/mcp") 覆盖路径导致 404）
     */
    private McpClientTransport buildHttpTransport(McpConfig config) {
        if (StrUtil.isBlank(config.getUrl())) {
            return null;
        }
        String baseUri = config.getUrl();
        String endpoint = DEFAULT_HTTP_ENDPOINT;
        try {
            URI uri = URI.create(config.getUrl());
            if (uri.getScheme() != null && uri.getAuthority() != null) {
                baseUri = uri.getScheme() + "://" + uri.getAuthority();
                endpoint = StrUtil.isEmpty(uri.getPath()) ? DEFAULT_HTTP_ENDPOINT : uri.getPath();
            }
        } catch (IllegalArgumentException e) {
            log.warn("[buildHttpTransport][服务地址({}) 解析失败，按原值使用]", config.getUrl(), e);
        }
        HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(baseUri)
                .endpoint(endpoint)
                .customizeClient(clientBuilder -> clientBuilder.connectTimeout(Duration.ofSeconds(10)));
        if (MapUtil.isNotEmpty(config.getHeaders())) {
            builder.httpRequestCustomizer(buildHeaderCustomizer(config.getHeaders()));
        }
        return builder.build();
    }

    /**
     * 本地进程传输：command + args + env
     */
    private McpClientTransport buildStdioTransport(McpConfig config) {
        if (StrUtil.isBlank(config.getCommand())) {
            return null;
        }
        ServerParameters.Builder params = ServerParameters.builder(config.getCommand());
        if (CollUtil.isNotEmpty(config.getArgs())) {
            params.args(config.getArgs());
        }
        if (MapUtil.isNotEmpty(config.getEnv())) {
            params.env(config.getEnv());
        }
        return new StdioClientTransport(params.build(), McpJsonDefaults.getMapper());
    }

    /**
     * 请求头自定义器：为 MCP HTTP 请求统一追加 headers
     */
    private static McpSyncHttpClientRequestCustomizer buildHeaderCustomizer(Map<String, String> headers) {
        return (builder, method, uri, protocolVersion, context) -> headers.forEach((key, value) -> {
            if (StrUtil.isNotEmpty(key) && value != null) {
                builder.header(key, value);
            }
        });
    }

    private static void closeQuietly(McpSyncClient client) {
        if (client == null) {
            return;
        }
        try {
            client.closeGracefully();
        } catch (Exception e) {
            log.warn("[closeQuietly][MCP 客户端关闭失败]", e);
        }
    }

    // ==================== 配置 / 结果解析 ====================

    /**
     * 解析 MCP 配置：transport 以列为准；url、headers 优先取 config，缺失时回退平铺列
     */
    private static McpConfig parseConfig(Ai1McpDO mcp) {
        Map<String, Object> configMap = MapUtil.emptyIfNull(JsonUtils.parseMap(mcp.getConfig()));
        McpConfig config = new McpConfig();
        config.setTransport(StrUtil.blankToDefault(mcp.getTransport(),
                StrUtil.blankToDefault(toStringValue(configMap.get("transport")), Ai1McpTransportEnum.HTTP.getTransport())));
        config.setUrl(StrUtil.blankToDefault(toStringValue(configMap.get("url")), mcp.getUrl()));
        Map<String, String> headers = parseStringMap(configMap.get("headers"));
        config.setHeaders(headers != null ? headers : parseStringMap(mcp.getHeaders()));
        config.setCommand(toStringValue(configMap.get("command")));
        if (configMap.get("args") instanceof List<?> args) {
            config.setArgs(new ArrayList<>());
            args.forEach(arg -> config.getArgs().add(String.valueOf(arg)));
        }
        config.setEnv(parseStringMap(configMap.get("env")));
        return config;
    }

    /**
     * 调用前解析地址、请求头、命令和环境变量里的 ${ENV}，库中的原文不改
     */
    private McpConfig resolveConfig(McpConfig config) {
        config.setUrl(Ai1ConfigPlaceholders.resolve(environment, config.getUrl()));
        config.setCommand(Ai1ConfigPlaceholders.resolve(environment, config.getCommand()));
        config.setHeaders(Ai1ConfigPlaceholders.resolveValues(environment, config.getHeaders()));
        config.setEnv(Ai1ConfigPlaceholders.resolveValues(environment, config.getEnv()));
        if (config.getArgs() != null) {
            List<String> args = new ArrayList<>();
            for (String arg : config.getArgs()) {
                args.add(Ai1ConfigPlaceholders.resolve(environment, arg));
            }
            config.setArgs(args);
        }
        return config;
    }

    // TODO @AI：hutool 有替代的方法么？或者简化的方法么？
    /**
     * 字符串 Map 解析（headers、env）：支持 JSON 对象，或 JSON 对象形式的字符串
     */
    private static Map<String, String> parseStringMap(Object value) {
        if (value instanceof String text) {
            value = JsonUtils.parseMap(text);
        }
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, item) -> {
            if (item != null && !(item instanceof Map) && !(item instanceof Collection)) {
                result.put(String.valueOf(key), String.valueOf(item));
            }
        });
        return result;
    }

    // TODO @AI：hutool 有替代的方法么？或者简化的方法么？
    private static String toStringValue(Object value) {
        return value == null || value instanceof Map || value instanceof Collection ? null : String.valueOf(value);
    }

    // TODO @AI：hutool 有替代的方法么？或者简化的方法么？
    /**
     * 工具调用结果文本提取：优先文本内容，其次结构化内容
     */
    private static String readText(McpSchema.CallToolResult result) {
        if (result == null) {
            return "";
        }
        if (CollUtil.isNotEmpty(result.content())) {
            StringBuilder text = new StringBuilder();
            for (McpSchema.Content content : result.content()) {
                if (content instanceof McpSchema.TextContent textContent) {
                    text.append(textContent.text());
                }
            }
            if (!text.isEmpty()) {
                return text.toString();
            }
        }
        if (result.structuredContent() != null) {
            return JsonUtils.toJsonString(result.structuredContent());
        }
        return String.valueOf(result);
    }

    /**
     * 配置指纹：传输方式、config、平铺列任一变化即失效重建
     */
    private static long buildFingerprint(Ai1McpDO mcp) {
        return Objects.hash(mcp.getId(), mcp.getTransport(), mcp.getConfig(), mcp.getUrl(), mcp.getHeaders());
    }

    // TODO @AI：data 策略 lomobok
    private record CachedClient(long fingerprint, McpSyncClient client) {
    }

    // TODO @AI：data 策略 lomobok
    private record CachedTools(long fingerprint, List<McpToolInfo> tools) {
    }

    /**
     * 解析后的 MCP 服务配置
     */
    @Data
    private static class McpConfig {

        /**
         * 传输方式
         */
        private String transport;
        /**
         * 服务地址
         */
        private String url;
        /**
         * 请求头
         */
        private Map<String, String> headers;
        /**
         * stdio 命令
         */
        private String command;
        /**
         * stdio 参数
         */
        private List<String> args;
        /**
         * stdio 环境变量
         */
        private Map<String, String> env;

    }

    /**
     * MCP 工具信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolInfo {

        /**
         * 工具名称
         */
        private String name;
        /**
         * 工具标题
         */
        private String title;
        /**
         * 工具描述
         */
        private String description;
        /**
         * 入参 JSON Schema；连通测试结果中为空
         */
        private Map<String, Object> inputSchema;

    }

    /**
     * MCP 连通测试结果
     */
    @Data
    public static class McpConnectResult {

        /**
         * 是否连通
         */
        private Boolean connectable;
        /**
         * 服务名称
         */
        private String serverName;
        /**
         * 服务版本
         */
        private String serverVersion;
        /**
         * 服务说明
         */
        private String instructions;
        /**
         * 可用工具数量
         */
        private Integer toolCount;
        /**
         * 测试耗时，单位：毫秒
         */
        private Long elapsedMs;
        /**
         * 测试过程描述
         */
        private String message;
        /**
         * 可用工具明细
         */
        private List<McpToolInfo> tools;

        public static McpConnectResult fail(String message, long startTime) {
            return new McpConnectResult().setConnectable(false).setToolCount(0).setTools(new ArrayList<>())
                    .setMessage(message).setElapsedMs(System.currentTimeMillis() - startTime);
        }

    }

}

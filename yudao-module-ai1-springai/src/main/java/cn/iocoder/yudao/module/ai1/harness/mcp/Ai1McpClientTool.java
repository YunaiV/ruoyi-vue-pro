package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpConnectRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.resolveSpringPlaceholders;

/**
 * AI1 MCP 客户端工具（官方 Java MCP SDK）：连接、列举工具、调用工具、连通测试
 *
 * 1. 传输层：http（Streamable HTTP）/ stdio（本地进程）；传输方式以 {@link Ai1McpDO#getTransport()} 列为准，
 *    其余参数取自 config JSON（缺失时回退平铺列）
 * 2. 客户端与工具列表按 MCP 编号 + 配置指纹缓存，配置变化时重建并关闭旧连接；闲置超时自动关闭
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
    /**
     * 缓存 key 分隔符
     */
    private static final String KEY_SEPARATOR = ":";
    /**
     * 缓存的闲置过期时间：超过该时间未使用的客户端自动关闭，释放连接与子进程
     *
     * 多节点下，其他节点删除 MCP 后，本节点的残留客户端也依赖该过期释放
     */
    private static final Duration CACHE_EXPIRE_AFTER_ACCESS = Duration.ofMinutes(30);

    /**
     * 客户端缓存：MCP 编号:配置指纹 → 客户端
     *
     * key 带上配置指纹，配置变化后自然命中不到旧客户端，不需要额外的缓存项对象
     */
    private final Cache<String, McpSyncClient> clientCache = CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_EXPIRE_AFTER_ACCESS)
            .removalListener((RemovalListener<String, McpSyncClient>) notification -> closeQuietly(notification.getValue()))
            .build();
    /**
     * 工具列表缓存：MCP 编号:配置指纹 → 工具列表
     */
    private final Cache<String, List<McpSchema.Tool>> toolsCache = CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_EXPIRE_AFTER_ACCESS)
            .build();

    /**
     * 获取 MCP 服务暴露的工具列表（带缓存，配置变化自动重建）
     *
     * 直接返回 SDK 的工具定义（含名称、描述、入参 JSON Schema），仅供 {@link Ai1McpToolFactory} 桥接 ToolCallback 使用
     *
     * @param mcp MCP 服务
     * @return 工具列表；列举失败时返回空列表
     */
    public List<McpSchema.Tool> listTools(Ai1McpDO mcp) {
        // 1. 命中缓存：配置指纹一致时，直接返回
        String cacheKey = buildCacheKey(mcp);
        List<McpSchema.Tool> cached = toolsCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 调用 tools/list 拉取工具列表；失败时记录日志，按空列表处理
        List<McpSchema.Tool> tools = new ArrayList<>();
        try {
            McpSchema.ListToolsResult result = getOrCreateClient(mcp).listTools();
            if (result != null && result.tools() != null) {
                tools = result.tools();
            }
        } catch (Exception e) {
            log.warn("[listTools][MCP({}/{}) tools/list 失败]", mcp.getId(), mcp.getName(), e);
        }

        // 3. 写入缓存（失败的空列表也缓存，避免每次对话都重试；配置变化后指纹不同会重新拉取）
        toolsCache.put(cacheKey, tools);
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
            McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest.builder(toolName)
                    .arguments(MapUtil.emptyIfNull(arguments)).build());
            return readText(result);
        }
    }

    /**
     * 连通测试：initialize + tools/list，使用临时客户端，不影响缓存
     *
     * @param mcp MCP 服务
     * @return 测试结果
     */
    public Ai1McpConnectRespVO testConnect(Ai1McpDO mcp) {
        long startTime = System.currentTimeMillis();
        McpSyncClient client = null;
        try {
            // 1. 建立连接
            McpClientTransport transport = buildTransport(mcp);
            if (transport == null) {
                return buildConnectFailResult("配置不完整（缺少必要参数）", startTime);
            }
            client = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(30))
                    .initializationTimeout(Duration.ofSeconds(15))
                    .build();
            client.initialize();

            // 2. 读取服务信息与工具列表
            Ai1McpConnectRespVO result = new Ai1McpConnectRespVO().setConnectable(true)
                    .setInstructions(client.getServerInstructions());
            McpSchema.Implementation serverInfo = client.getServerInfo();
            if (serverInfo != null) {
                result.setServerName(serverInfo.name()).setServerVersion(serverInfo.version());
            }
            McpSchema.ListToolsResult listResult = client.listTools();
            result.setTools(convertList(listResult != null ? listResult.tools() : null, tool -> new Ai1McpConnectRespVO.Tool()
                    .setName(tool.name()).setTitle(tool.title()).setDescription(tool.description())));
            return result.setToolCount(result.getTools().size())
                    .setMessage("连接成功，发现 " + result.getTools().size() + " 个工具")
                    .setElapsedMs(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.warn("[testConnect][MCP({}/{}) 连通测试失败]", mcp.getId(), mcp.getName(), e);
            return buildConnectFailResult(String.valueOf(e.getMessage()), startTime);
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
        String prefix = mcpId + KEY_SEPARATOR;
        toolsCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        clientCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    @PreDestroy
    public void destroy() {
        toolsCache.invalidateAll();
        clientCache.invalidateAll();
    }

    // ==================== 连接 / 传输 ====================

    /**
     * 获取（或构建）MCP 客户端：配置指纹变化时重建，旧连接由缓存的 RemovalListener 关闭
     */
    private McpSyncClient getOrCreateClient(Ai1McpDO mcp) {
        // 1. 命中缓存：配置指纹一致时，直接复用
        String cacheKey = buildCacheKey(mcp);
        McpSyncClient cached = clientCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (clientCache) {
            cached = clientCache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }
            // 2. 配置变化：先释放该 MCP 旧指纹的客户端，再构建新的
            evict(mcp.getId());
            McpClientTransport transport = buildTransport(mcp);
            if (transport == null) {
                throw new IllegalStateException("MCP(" + mcp.getName() + ") 配置不完整（缺少必要参数）");
            }
            McpSyncClient client = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(60))
                    .initializationTimeout(Duration.ofSeconds(15))
                    .build();
            client.initialize();
            clientCache.put(cacheKey, client);
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
                StrUtil.blankToDefault(MapUtil.getStr(configMap, "transport"), Ai1McpTransportEnum.HTTP.getTransport())));
        config.setUrl(StrUtil.blankToDefault(MapUtil.getStr(configMap, "url"), mcp.getUrl()));
        Map<String, String> headers = parseStringMap(configMap.get("headers"));
        config.setHeaders(headers != null ? headers : mcp.getHeaders());
        config.setCommand(MapUtil.getStr(configMap, "command"));
        config.setArgs(Convert.toList(String.class, configMap.get("args")));
        config.setEnv(parseStringMap(configMap.get("env")));
        return config;
    }

    /**
     * 调用前解析地址、请求头、命令和环境变量里的 ${ENV}，库中的原文不改
     */
    private static McpConfig resolveConfig(McpConfig config) {
        config.setUrl(resolveSpringPlaceholders(config.getUrl()));
        config.setCommand(resolveSpringPlaceholders(config.getCommand()));
        config.setHeaders(resolveSpringPlaceholders(config.getHeaders()));
        config.setEnv(resolveSpringPlaceholders(config.getEnv()));
        config.setArgs(convertList(config.getArgs(), arg -> resolveSpringPlaceholders(arg)));
        return config;
    }

    /**
     * 字符串 Map 解析（config 中的 headers、env）：支持 JSON 对象，或 JSON 对象形式的字符串
     */
    private static Map<String, String> parseStringMap(Object value) {
        if (value instanceof String) {
            value = JsonUtils.parseMap((String) value);
        }
        return value instanceof Map ? MapUtil.removeNullValue(Convert.toMap(String.class, String.class, value)) : null;
    }

    /**
     * 工具调用结果文本提取：优先文本内容，其次结构化内容
     */
    private static String readText(McpSchema.CallToolResult result) {
        if (result == null) {
            return "";
        }
        String text = CollUtil.join(convertList(result.content(), content -> content instanceof McpSchema.TextContent
                ? ((McpSchema.TextContent) content).text() : null), "");
        if (StrUtil.isNotEmpty(text)) {
            return text;
        }
        if (result.structuredContent() != null) {
            return JsonUtils.toJsonString(result.structuredContent());
        }
        return String.valueOf(result);
    }

    /**
     * 缓存 key：MCP 编号 + 配置指纹，传输方式、config、平铺列任一变化即得到新的 key
     *
     * 多节点下，各节点加载到的 MCP 配置变化后指纹不同，自然重建客户端
     */
    private static String buildCacheKey(Ai1McpDO mcp) {
        return mcp.getId() + KEY_SEPARATOR
                + Objects.hash(mcp.getTransport(), mcp.getConfig(), mcp.getUrl(), mcp.getHeaders());
    }

    /**
     * 构建连通测试失败结果
     */
    private static Ai1McpConnectRespVO buildConnectFailResult(String message, long startTime) {
        return new Ai1McpConnectRespVO().setConnectable(false).setToolCount(0).setTools(new ArrayList<>())
                .setMessage(message).setElapsedMs(System.currentTimeMillis() - startTime);
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

}

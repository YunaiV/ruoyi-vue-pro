package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.service.mcp.Ai1McpService;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link Ai1McpToolFactory} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1McpToolFactoryTest extends BaseMockitoUnitTest {

    @InjectMocks
    private Ai1McpToolFactory mcpToolFactory;

    @Mock
    private Ai1McpService mcpService;
    @Mock
    private Ai1McpClientTool mcpClientTool;

    @Test
    public void testBuildTools_chineseNames() {
        // mock 数据
        Ai1McpDO first = buildMcp(1L, "高德地图");
        Ai1McpDO second = buildMcp(2L, "百度地图");

        // 调用
        List<ToolCallback> callbacks = buildTools(List.of(first, second), List.of("search"));
        // 断言
        assertEquals(List.of("mcp_1__search", "mcp_2__search"), callbacks.stream()
                .map(callback -> callback.getToolDefinition().name()).toList());
    }

    @Test
    public void testBuildTools_emptyNames() {
        // mock 数据
        List<Ai1McpDO> mcps = List.of(buildMcp(1L, "   "), buildMcp(2L, "___"), buildMcp(3L, "!!!"));

        // 调用
        List<ToolCallback> callbacks = buildTools(mcps, List.of("search"));
        // 断言
        assertEquals(List.of("mcp_1__search", "mcp_2__search", "mcp_3__search"), callbacks.stream()
                .map(callback -> callback.getToolDefinition().name()).toList());
    }

    @Test
    public void testBuildTools_englishName() {
        // mock 数据
        Ai1McpDO mcp = buildMcp(1L, "GitHub");

        // 调用
        List<ToolCallback> callbacks = buildTools(List.of(mcp), List.of("search"));
        // 断言
        assertEquals("github__search", callbacks.getFirst().getToolDefinition().name());
    }

    @Test
    public void testBuildTools_nameAtLimit() {
        // mock 数据
        Ai1McpDO mcp = buildMcp(1L, "a".repeat(56));

        // 调用
        List<ToolCallback> callbacks = buildTools(List.of(mcp), List.of("search"));
        // 断言
        assertEquals("a".repeat(56) + "__search", callbacks.getFirst().getToolDefinition().name());
        assertEquals(64, callbacks.getFirst().getToolDefinition().name().length());
    }

    @Test
    public void testBuildTools_longToolNames() {
        // mock 数据
        Ai1McpDO mcp = buildMcp(1L, "GitHub");
        String prefix = "search_" + "a".repeat(80);

        // 调用
        List<ToolCallback> callbacks = buildTools(List.of(mcp), List.of(prefix + "_first", prefix + "_second"));
        // 断言
        String firstName = callbacks.get(0).getToolDefinition().name();
        String secondName = callbacks.get(1).getToolDefinition().name();
        assertEquals(64, firstName.length());
        assertEquals(64, secondName.length());
        assertTrue(firstName.matches("[a-zA-Z0-9_-]+"));
        assertNotEquals(firstName, secondName);
    }

    @Test
    public void testBuildTools_longMcpNames() {
        // mock 数据
        String prefix = "a".repeat(80);
        Ai1McpDO first = buildMcp(1L, prefix + "_first");
        Ai1McpDO second = buildMcp(2L, prefix + "_second");

        // 调用
        List<ToolCallback> callbacks = buildTools(List.of(first, second), List.of("search"));
        // 断言
        String firstName = callbacks.get(0).getToolDefinition().name();
        String secondName = callbacks.get(1).getToolDefinition().name();
        assertEquals(64, firstName.length());
        assertEquals(64, secondName.length());
        assertNotEquals(firstName, secondName);
    }

    @Test
    public void testBuildTools_callOriginalTool() {
        // mock 数据
        Ai1McpDO mcp = buildMcp(1L, "高德地图");
        String toolName = "search_" + "a".repeat(80);
        Map<String, Object> arguments = Map.of("query", "北京");
        // mock mcpClientTool 的方法
        when(mcpClientTool.callTool(mcp, toolName, arguments)).thenReturn("搜索结果");
        List<ToolCallback> callbacks = buildTools(List.of(mcp), List.of(toolName));

        // 调用
        String result = callbacks.getFirst().call("{\"query\":\"北京\"}");
        // 断言
        assertEquals("搜索结果", JsonUtils.parseObject(result, String.class));
        verify(mcpClientTool).callTool(mcp, toolName, arguments);
    }

    private List<ToolCallback> buildTools(List<Ai1McpDO> mcps, List<String> toolNames) {
        Ai1AgentDO agent = new Ai1AgentDO().setId(1L).setMcpIds(mcps.stream().map(Ai1McpDO::getId).toList());
        when(mcpService.getMcpList(agent.getMcpIds())).thenReturn(mcps);
        List<McpSchema.Tool> tools = toolNames.stream().map(name -> McpSchema.Tool.builder(name)
                .description("测试工具").inputSchema(Map.of("type", "object")).build()).toList();
        for (Ai1McpDO mcp : mcps) {
            when(mcpClientTool.listTools(mcp)).thenReturn(tools);
        }
        return mcpToolFactory.buildTools(agent);
    }

    private static Ai1McpDO buildMcp(Long id, String name) {
        return new Ai1McpDO().setId(id).setName(name).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}

package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpTransportException;
import io.modelcontextprotocol.spec.McpTransportSessionClosedException;
import io.modelcontextprotocol.spec.McpTransportSessionNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1McpClientTool} 的故障恢复单元测试
 *
 * @author 芋道源码
 */
public class Ai1McpClientToolRecoveryTest extends BaseMockitoUnitTest {

    @InjectMocks
    private Ai1McpClientTool mcpClientTool;

    @Mock
    private McpSyncClient failedClient;
    @Mock
    private McpSyncClient readyClient;

    @AfterEach
    public void after() {
        mcpClientTool.destroy();
    }

    @Test
    public void testListTools_initializeFailureCanRetry() {
        // mock failedClient 和 readyClient 的方法
        when(failedClient.initialize()).thenThrow(new IllegalStateException("建连失败"));
        List<McpSchema.Tool> tools = buildTools();
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        // 调用，并断言
        assertTrue(listTools(mcp, failedClient).isEmpty());
        verify(failedClient).closeGracefully();
        assertEquals(tools, listTools(mcp, readyClient));
        assertEquals(tools, listTools(mcp, readyClient));
        verify(readyClient).initialize();
        verify(readyClient).listTools();
    }

    @Test
    public void testListTools_listFailureCanRetry() {
        // mock readyClient 的方法
        List<McpSchema.Tool> tools = buildTools();
        when(readyClient.listTools()).thenThrow(new IllegalStateException("列举失败"))
                .thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        // 调用，并断言
        assertTrue(listTools(mcp, readyClient).isEmpty());
        assertEquals(tools, listTools(mcp, readyClient));
        assertEquals(tools, listTools(mcp, readyClient));
        verify(readyClient).initialize();
        verify(readyClient, times(2)).listTools();
    }

    @Test
    public void testListTools_successfulEmptyResultIsCached() {
        // mock readyClient 的方法
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(List.of(), null, null));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        // 调用，并断言
        assertTrue(listTools(mcp, readyClient).isEmpty());
        assertTrue(listTools(mcp, readyClient).isEmpty());
        verify(readyClient).initialize();
        verify(readyClient).listTools();
    }

    @Test
    public void testListTools_closeFailureDoesNotPreventRetry() {
        // mock failedClient 和 readyClient 的方法
        when(failedClient.initialize()).thenThrow(new IllegalStateException("建连失败"));
        doThrow(new IllegalStateException("关闭失败")).when(failedClient).closeGracefully();
        List<McpSchema.Tool> tools = buildTools();
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        // 调用，并断言
        assertTrue(listTools(mcp, failedClient).isEmpty());
        assertEquals(tools, listTools(mcp, readyClient));
        verify(failedClient).closeGracefully();
        verify(readyClient).initialize();
    }

    @ParameterizedTest
    @MethodSource("callFailures")
    public void testCallTool_failureRebuildsClient(RuntimeException failure) {
        // mock failedClient 和 readyClient 的方法
        List<McpSchema.Tool> tools = buildTools();
        when(failedClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(failedClient.callTool(any())).thenAnswer(invocation -> {
            throw failure;
        });
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(readyClient.callTool(any())).thenReturn(buildResult(false));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        withClients(() -> {
            // 调用，并断言失败原样返回，本次不自动重跑
            assertEquals(tools, mcpClientTool.listTools(mcp));
            assertSame(failure, assertThrows(RuntimeException.class,
                    () -> mcpClientTool.callTool(mcp, "calculator", null)));
            verify(failedClient).callTool(any());
            verify(failedClient).closeGracefully();
            verify(readyClient, never()).callTool(any());
            // 调用，并断言工具缓存已清理，下次列举和调用使用新客户端
            assertEquals(tools, mcpClientTool.listTools(mcp));
            assertEquals("工具结果", mcpClientTool.callTool(mcp, "calculator", Map.of()));
            verify(readyClient).initialize();
            verify(readyClient).listTools();
            verify(readyClient).callTool(any());
            return null;
        }, failedClient, readyClient);
    }

    @Test
    public void testCallTool_businessErrorKeepsClient() {
        // mock readyClient 的方法
        McpError failure = McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS).message("参数错误").build();
        List<McpSchema.Tool> tools = buildTools();
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(readyClient.callTool(any())).thenThrow(failure).thenReturn(buildResult(false));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        withClients(() -> {
            // 调用，并断言业务错误保留客户端和工具缓存
            assertEquals(tools, mcpClientTool.listTools(mcp));
            assertSame(failure, assertThrows(McpError.class,
                    () -> mcpClientTool.callTool(mcp, "calculator", Map.of())));
            assertEquals(tools, mcpClientTool.listTools(mcp));
            assertEquals("工具结果", mcpClientTool.callTool(mcp, "calculator", Map.of()));
            verify(readyClient).initialize();
            verify(readyClient).listTools();
            verify(readyClient, never()).closeGracefully();
            return null;
        }, readyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testCallTool_resultKeepsClient(boolean isError) {
        // mock readyClient 的方法
        when(readyClient.callTool(any())).thenReturn(buildResult(isError));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        withClients(() -> {
            // 调用，并断言工具结果（含 isError）均不会断开连接
            assertEquals("工具结果", mcpClientTool.callTool(mcp, "calculator", Map.of()));
            assertEquals("工具结果", mcpClientTool.callTool(mcp, "calculator", Map.of()));
            verify(readyClient).initialize();
            verify(readyClient, times(2)).callTool(any());
            verify(readyClient, never()).closeGracefully();
            return null;
        }, readyClient);
    }

    @Test
    public void testCallTool_closeFailureKeepsOriginalError() {
        // mock failedClient 和 readyClient 的方法
        McpTransportException failure = new McpTransportException("连接断开");
        when(failedClient.callTool(any())).thenThrow(failure);
        doThrow(new IllegalStateException("关闭失败")).when(failedClient).closeGracefully();
        when(readyClient.callTool(any())).thenReturn(buildResult(false));
        // 准备参数
        Ai1McpDO mcp = buildMcp();

        withClients(() -> {
            // 调用，并断言释放失败不掩盖调用错误，也不妨碍下次重建
            assertSame(failure, assertThrows(McpTransportException.class,
                    () -> mcpClientTool.callTool(mcp, "calculator", Map.of())));
            assertEquals("工具结果", mcpClientTool.callTool(mcp, "calculator", Map.of()));
            verify(failedClient).closeGracefully();
            verify(readyClient).initialize();
            return null;
        }, failedClient, readyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testCallTool_lateFailureKeepsNewClient(boolean configChanged) {
        // mock failedClient 和 readyClient 的方法
        List<McpSchema.Tool> tools = buildTools();
        when(failedClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(readyClient.callTool(any())).thenReturn(buildResult(false));
        // 准备参数
        Ai1McpDO mcp = buildMcp();
        Ai1McpDO newMcp = buildMcp();
        if (configChanged) {
            newMcp.setUrl("http://127.0.0.1:8082/mcp");
        }
        McpTransportException failure = new McpTransportException("旧连接断开");
        when(failedClient.callTool(any())).thenAnswer(invocation -> {
            // mock 在途调用期间被配置更新或手动清理，随后已重建新客户端
            if (!configChanged) {
                mcpClientTool.evict(mcp.getId());
            }
            assertEquals(tools, mcpClientTool.listTools(newMcp));
            throw failure;
        });

        withClients(() -> {
            // 调用，并断言旧调用失败不能清理新客户端或新工具缓存
            assertEquals(tools, mcpClientTool.listTools(mcp));
            assertSame(failure, assertThrows(McpTransportException.class,
                    () -> mcpClientTool.callTool(mcp, "calculator", Map.of())));
            assertEquals(tools, mcpClientTool.listTools(newMcp));
            assertEquals("工具结果", mcpClientTool.callTool(newMcp, "calculator", Map.of()));
            verify(failedClient).closeGracefully();
            verify(readyClient).initialize();
            verify(readyClient).listTools();
            verify(readyClient, never()).closeGracefully();
            return null;
        }, failedClient, readyClient);
    }

    @Test
    public void testCallTool_failureKeepsOtherMcpClient() {
        // mock failedClient 和 readyClient 的方法
        McpTransportException failure = new McpTransportException("连接断开");
        when(failedClient.callTool(any())).thenThrow(failure);
        List<McpSchema.Tool> tools = buildTools();
        when(readyClient.listTools()).thenReturn(new McpSchema.ListToolsResult(tools, null, null));
        when(readyClient.callTool(any())).thenReturn(buildResult(false));
        // 准备参数
        Ai1McpDO mcp = buildMcp();
        Ai1McpDO otherMcp = buildMcp().setId(2L);

        withClients(() -> {
            // 调用，并断言某个 MCP 失败不影响其他服务的连接与工具缓存
            assertThrows(McpTransportException.class, () -> mcpClientTool.callTool(mcp, "calculator", Map.of()));
            assertEquals(tools, mcpClientTool.listTools(otherMcp));
            assertThrows(McpTransportException.class, () -> mcpClientTool.callTool(mcp, "calculator", Map.of()));
            assertEquals(tools, mcpClientTool.listTools(otherMcp));
            assertEquals("工具结果", mcpClientTool.callTool(otherMcp, "calculator", Map.of()));
            verify(readyClient).initialize();
            verify(readyClient).listTools();
            verify(readyClient, never()).closeGracefully();
            return null;
        }, failedClient, readyClient, failedClient);
    }

    // ========== 测试数据 ==========

    private static Ai1McpDO buildMcp() {
        return new Ai1McpDO().setId(1L).setName("测试服务").setTransport("http")
                .setUrl("http://127.0.0.1:8081/mcp");
    }

    private static List<McpSchema.Tool> buildTools() {
        return List.of(McpSchema.Tool.builder("calculator").description("计算器")
                .inputSchema(Map.of("type", "object")).build());
    }

    private static McpSchema.CallToolResult buildResult(boolean isError) {
        return McpSchema.CallToolResult.builder().addTextContent("工具结果").isError(isError).build();
    }

    private static Stream<RuntimeException> callFailures() {
        return Stream.of(new McpTransportSessionClosedException(), new McpTransportSessionNotFoundException("session"),
                new McpTransportException("HTTP 连接失败"), new RuntimeException("Failed to enqueue message"),
                new RuntimeException("MCP session with server terminated"),
                new RuntimeException(new IOException("管道已关闭")),
                reactor.core.Exceptions.propagate(new TimeoutException("工具调用超时")));
    }

    // ========== mock 方法 ==========

    private List<McpSchema.Tool> listTools(Ai1McpDO mcp, McpSyncClient client) {
        return withClients(() -> mcpClientTool.listTools(mcp), client);
    }

    private <T> T withClients(Supplier<T> action, McpSyncClient... clients) {
        List<McpClientTransport> transports = new ArrayList<>();
        McpClient.SyncSpec spec = mock(McpClient.SyncSpec.class, RETURNS_SELF);
        lenient().when(spec.build()).thenReturn(clients[0], Arrays.copyOfRange(clients, 1, clients.length));
        try (MockedStatic<McpClient> clientFactory = mockStatic(McpClient.class)) {
            clientFactory.when(() -> McpClient.sync(any(McpClientTransport.class))).thenAnswer(invocation -> {
                transports.add(invocation.getArgument(0));
                return spec;
            });
            return action.get();
        } finally {
            for (McpClientTransport transport : transports) {
                transport.closeGracefully().block(Duration.ofSeconds(5));
            }
        }
    }

}

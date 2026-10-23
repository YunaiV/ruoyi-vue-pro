package cn.iocoder.yudao.module.ai1.harness.llm;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.function.FunctionToolCallback;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1LlmChatTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1LlmChatToolTest extends BaseMockitoUnitTest {

    @InjectMocks
    private Ai1LlmChatTool llmChatTool;
    @Mock
    private Ai1LlmModelFactory llmModelFactory;

    @Test
    public void testChat_mixedTools() {
        // 准备参数
        AnnotatedTools annotatedTools = new AnnotatedTools();
        AtomicInteger mcpCalls = new AtomicInteger();
        AtomicInteger skillCalls = new AtomicInteger();
        List<Object> tools = java.util.Arrays.asList(annotatedTools,
                FunctionToolCallback.builder("mcp_probe", (String value) -> {
                    mcpCalls.incrementAndGet();
                    return "mcp:" + value;
                }).inputType(String.class).description("MCP 回调").build(),
                FunctionToolCallback.builder("skill_probe", (String value) -> {
                    skillCalls.incrementAndGet();
                    return "skill:" + value;
                }).inputType(String.class).description("SKILL 回调").build());

        // mock chatModel 和 llmModelFactory 的方法
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.getDefaultOptions()).thenReturn(ToolCallingChatOptions.builder().build());
        ToolCallingManager manager = ToolCallingManager.builder().build();
        when(chatModel.stream(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            ToolCallingChatOptions options = (ToolCallingChatOptions) prompt.getOptions();
            assertEquals(3, options.getToolCallbacks().size());
            assertEquals(3, manager.resolveToolDefinitions(options).size());
            AssistantMessage toolCalls = AssistantMessage.builder().content("").toolCalls(java.util.Arrays.asList(
                    new AssistantMessage.ToolCall("1", "function", "annotated_probe", "{}"),
                    new AssistantMessage.ToolCall("2", "function", "mcp_probe", "\"hello\""),
                    new AssistantMessage.ToolCall("3", "function", "skill_probe", "\"hello\""))).build();
            ChatResponse response = new ChatResponse(java.util.Arrays.asList(new Generation(toolCalls)));
            List<ToolResponseMessage.ToolResponse> results = manager.executeToolCalls(prompt, response)
                    .conversationHistory().stream().filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast).flatMap(message -> message.getResponses().stream()).toList();
            assertEquals(3, results.size());
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("annotated")));
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("mcp:hello")));
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("skill:hello")));
            return Flux.just(new ChatResponse(java.util.Arrays.asList(new Generation(new AssistantMessage("工具调用成功")))));
        });
        ChatClient chatClient = ChatClient.create(chatModel);
        when(llmModelFactory.buildChatClient(any(), any())).thenReturn(chatClient);

        // 调用
        Ai1LlmChatTool.ChatText result = llmChatTool.chat(new Ai1ModelRespBO(), "", java.util.Arrays.asList(), "调用工具",
                tools, java.util.Arrays.asList(), 1L, null, null);
        // 断言
        assertEquals("工具调用成功", result.getContent());
        assertEquals(1, annotatedTools.calls);
        assertEquals(1, mcpCalls.get());
        assertEquals(1, skillCalls.get());
    }

    @Test
    public void testChat_whitespaceChunks() {
        // mock chatModel 和 llmModelFactory 的方法：空格、换行分片也是有效增量，只跳过空串
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.getDefaultOptions()).thenReturn(ToolCallingChatOptions.builder().build());
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(
                buildChunk("想", "你好"), buildChunk(" ", " "), buildChunk("\n", "\n"),
                buildChunk("", "世界"), buildChunk(null, "")));
        ChatClient chatClient = ChatClient.create(chatModel);
        when(llmModelFactory.buildChatClient(any(), any())).thenReturn(chatClient);
        // 准备参数
        List<String> thinkingDeltas = new ArrayList<>();
        List<String> contentDeltas = new ArrayList<>();

        // 调用
        Ai1LlmChatTool.ChatText result = llmChatTool.chat(new Ai1ModelRespBO(), "", java.util.Arrays.asList(), "你好",
                java.util.Arrays.asList(), java.util.Arrays.asList(), 1L, thinkingDeltas::add, contentDeltas::add);
        // 断言
        assertEquals("你好 \n世界", result.getContent());
        assertEquals("想 \n", result.getThinking());
        assertEquals(java.util.Arrays.asList("你好", " ", "\n", "世界"), contentDeltas);
        assertEquals(java.util.Arrays.asList("想", " ", "\n"), thinkingDeltas);
    }

    // ========== 测试数据 ==========

    private static ChatResponse buildChunk(String reasoning, String content) {
        Map<String, Object> properties = new HashMap<>();
        if (reasoning != null) {
            properties.put("reasoningContent", reasoning);
        }
        return new ChatResponse(java.util.Arrays.asList(new Generation(AssistantMessage.builder()
                .content(content).properties(properties).build())));
    }

    // ========== 测试工具 ==========

    public static class AnnotatedTools {

        private int calls;

        @Tool(name = "annotated_probe", description = "注解工具")
        public String probe() {
            calls++;
            return "annotated";
        }

    }

}

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

import java.util.List;
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
        List<Object> tools = List.of(annotatedTools,
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
        when(chatModel.getOptions()).thenReturn(ToolCallingChatOptions.builder().build());
        ToolCallingManager manager = ToolCallingManager.builder().build();
        when(chatModel.stream(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            ToolCallingChatOptions options = (ToolCallingChatOptions) prompt.getOptions();
            assertEquals(3, options.getToolCallbacks().size());
            assertEquals(3, manager.resolveToolDefinitions(options).size());
            AssistantMessage toolCalls = AssistantMessage.builder().content("").toolCalls(List.of(
                    new AssistantMessage.ToolCall("1", "function", "annotated_probe", "{}"),
                    new AssistantMessage.ToolCall("2", "function", "mcp_probe", "\"hello\""),
                    new AssistantMessage.ToolCall("3", "function", "skill_probe", "\"hello\""))).build();
            ChatResponse response = new ChatResponse(List.of(new Generation(toolCalls)));
            List<ToolResponseMessage.ToolResponse> results = manager.executeToolCalls(prompt, response)
                    .conversationHistory().stream().filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast).flatMap(message -> message.getResponses().stream()).toList();
            assertEquals(3, results.size());
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("annotated")));
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("mcp:hello")));
            assertTrue(results.stream().anyMatch(result -> result.responseData().contains("skill:hello")));
            return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage("工具调用成功")))));
        });
        ChatClient chatClient = ChatClient.create(chatModel);
        when(llmModelFactory.buildChatClient(any(), any())).thenReturn(chatClient);

        // 调用
        Ai1LlmChatTool.ChatText result = llmChatTool.chat(new Ai1ModelRespBO(), "", List.of(), "调用工具",
                tools, List.of(), 1L, null, null);
        // 断言
        assertEquals("工具调用成功", result.getContent());
        assertEquals(1, annotatedTools.calls);
        assertEquals(1, mcpCalls.get());
        assertEquals(1, skillCalls.get());
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

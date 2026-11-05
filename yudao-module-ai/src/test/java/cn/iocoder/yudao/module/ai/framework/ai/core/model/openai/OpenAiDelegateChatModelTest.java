package cn.iocoder.yudao.module.ai.framework.ai.core.model.openai;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.grok.GrokChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiChatModel;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OpenAiDelegateChatModel} 的单元测试类
 *
 * 累计→增量的各种边界（连续相同片段、工具轮次、并发、空思考）见 TongYiChatModelTest，这里覆盖 OpenAI、Grok 与工厂入口
 *
 * @author 芋道源码
 */
public class OpenAiDelegateChatModelTest {

    private final ChatModel openAiChatModel = mock(ChatModel.class);

    @Test
    public void testStream_openAi() {
        // 准备参数
        OpenAiDelegateChatModel chatModel = new OpenAiDelegateChatModel(openAiChatModel);
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        // mock 方法：上游增量 a、b、c，Spring AI 2.x 写入的累计值为 a、ab、abc，之后正文 chunk 仍带 abc
        when(openAiChatModel.stream(same(prompt))).thenReturn(buildCumulativeChunks());

        // 调用：按业务方式逐 chunk 取思考内容
        List<ChatResponse> responses = chatModel.stream(prompt).collectList().block();
        List<String> reasoningContents = responses.stream().map(AiChatUtils::getChatResponseReasoningContent).toList();
        // 断言：增量不重复，拼接为 abc
        assertEquals(List.of("a", "b", "c", "", ""), reasoningContents);
        assertEquals("abc", String.join("", reasoningContents));
        // 断言：累计值保持不变
        assertEquals("abc", responses.get(4).getResult().getOutput().getMetadata()
                .get(OpenAiDelegateChatModel.REASONING_CONTENT));
    }

    @Test
    public void testStream_grok() {
        // 准备参数
        GrokChatModel chatModel = new GrokChatModel(openAiChatModel);
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        // mock 方法
        when(openAiChatModel.stream(same(prompt))).thenReturn(buildCumulativeChunks());

        // 调用
        String reasoningContent = String.join("", chatModel.stream(prompt)
                .map(AiChatUtils::getChatResponseReasoningContent).collectList().block());
        // 断言：Grok 复用同一处理，拼接结果不重复
        assertEquals("abc", reasoningContent);
    }

    @Test
    public void testStream_aggregateKeepsReasoningContent() {
        // 准备参数
        OpenAiDelegateChatModel chatModel = new OpenAiDelegateChatModel(openAiChatModel);
        Prompt prompt = new Prompt(new UserMessage("查下天气"));
        // mock 方法
        when(openAiChatModel.stream(same(prompt))).thenReturn(buildCumulativeChunks());

        // 调用：与 ChatClient 工具调用一样，按 last-wins 聚合本轮的 chunk
        AtomicReference<ChatResponse> aggregated = new AtomicReference<>();
        new MessageAggregator().aggregate(chatModel.stream(prompt), aggregated::set).blockLast();
        // 断言：聚合后的完整思考仍是累计值，续调用回传的 reasoning_content 不丢
        assertEquals("abc", aggregated.get().getResult().getOutput().getMetadata()
                .get(OpenAiDelegateChatModel.REASONING_CONTENT));
    }

    @Test
    public void testCall() {
        // 准备参数
        OpenAiDelegateChatModel chatModel = new OpenAiDelegateChatModel(openAiChatModel);
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        ChatResponse response = buildChunk("c1", "2", "abc");
        // mock 方法
        when(openAiChatModel.call(same(prompt))).thenReturn(response);

        // 调用
        ChatResponse result = chatModel.call(prompt);
        // 断言：同步原样返回，思考内容取完整值
        assertSame(response, result);
        assertEquals("abc", AiChatUtils.getChatResponseReasoningContent(result));
    }

    @Test
    public void testGetOrCreateChatModel_openAiRoutes() {
        // 准备参数
        AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

        // 调用，并断言：数据库动态入口中，复用 OpenAiChatModel 的平台都经过 OpenAiDelegateChatModel
        assertInstanceOf(OpenAiDelegateChatModel.class, modelFactory.getOrCreateChatModel(AiPlatformEnum.OPENAI,
                "sk-test", "http://127.0.0.1:1/v1", null));
        assertInstanceOf(GrokChatModel.class, modelFactory.getOrCreateChatModel(AiPlatformEnum.GROK,
                "sk-test", "http://127.0.0.1:1/v1", null));
        assertInstanceOf(TongYiChatModel.class, modelFactory.getOrCreateChatModel(AiPlatformEnum.TONG_YI,
                "sk-test", null, null));
    }

    // ========== 构造 chunk ==========

    /**
     * 构造 Spring AI 2.x OpenAiChatModel 的流式 chunk：上游增量 a、b、c，之后两个正文 chunk
     */
    private static Flux<ChatResponse> buildCumulativeChunks() {
        return Flux.just(buildChunk("c1", "", "a"), buildChunk("c1", "", "ab"), buildChunk("c1", "", "abc"),
                buildChunk("c1", "测试", "abc"), buildChunk("c1", "成功", "abc"));
    }

    private static ChatResponse buildChunk(String id, String content, String reasoningContent) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id);
        metadata.put("index", 0);
        metadata.put(OpenAiDelegateChatModel.REASONING_CONTENT, reasoningContent);
        return new ChatResponse(List.of(new Generation(AssistantMessage.builder()
                .content(content).properties(metadata).build())));
    }

}

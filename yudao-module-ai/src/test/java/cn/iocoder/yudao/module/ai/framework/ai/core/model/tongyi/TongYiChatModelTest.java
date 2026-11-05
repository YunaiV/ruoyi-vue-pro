package cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link TongYiChatModel} 的单元测试类
 *
 * mock 被复用的 OpenAiChatModel，按 Spring AI 2.x 流式的真实形态构造 chunk：metadata 的 reasoningContent 为累计值
 *
 * @author 芋道源码
 */
public class TongYiChatModelTest {

    private final ChatModel openAiChatModel = mock(ChatModel.class);

    private final TongYiChatModel chatModel = new TongYiChatModel(openAiChatModel);

    @Test
    public void testStream_reasoningContentDelta() {
        // 准备参数
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        // mock 方法：累计值 A、AB、AB（最后一个 chunk 只有正文）
        when(openAiChatModel.stream(same(prompt))).thenReturn(Flux.just(
                buildChunk("c1", "", "A"), buildChunk("c1", "", "AB"), buildChunk("c1", "2", "AB")));

        // 调用
        List<ChatResponse> responses = chatModel.stream(prompt).collectList().block();
        // 断言：增量
        assertEquals(List.of("A", "B", ""), getReasoningContentDeltas(responses));
        // 断言：累计值、正文、其余 metadata 保持不变
        assertEquals("AB", getMetadata(responses.get(2)).get(OpenAiDelegateChatModel.REASONING_CONTENT));
        assertEquals("2", responses.get(2).getResult().getOutput().getText());
        assertEquals("c1", getMetadata(responses.get(2)).get("id"));
    }

    @Test
    public void testStream_repeatedFragment() {
        // 准备参数
        Prompt prompt = new Prompt(new UserMessage("笑一个"));
        // mock 方法：连续三个合法的相同片段「哈」
        when(openAiChatModel.stream(same(prompt))).thenReturn(Flux.just(
                buildChunk("c1", "", "哈"), buildChunk("c1", "", "哈哈"), buildChunk("c1", "", "哈哈哈")));

        // 调用
        List<ChatResponse> responses = chatModel.stream(prompt).collectList().block();
        // 断言：不按字符串相等去重，每个片段都保留
        assertEquals(List.of("哈", "哈", "哈"), getReasoningContentDeltas(responses));
    }

    @Test
    public void testStream_eachCallIsolated() {
        // 准备参数：同一个 Prompt 调用两次，模拟 ChatClient 工具调用的两轮
        Prompt prompt = new Prompt(new UserMessage("查下天气"));
        // mock 方法：两轮的 id 相同，第二轮的累计值从头开始
        when(openAiChatModel.stream(same(prompt))).thenReturn(
                Flux.just(buildChunk("c1", "", "A"), buildChunk("c1", "", "AB")),
                Flux.just(buildChunk("c1", "", "AB"), buildChunk("c1", "", "ABC")));

        // 调用
        List<ChatResponse> round1 = chatModel.stream(prompt).collectList().block();
        List<ChatResponse> round2 = chatModel.stream(prompt).collectList().block();
        // 断言：第二轮不沿用第一轮的累计值
        assertEquals(List.of("A", "B"), getReasoningContentDeltas(round1));
        assertEquals(List.of("AB", "C"), getReasoningContentDeltas(round2));
    }

    @Test
    public void testStream_concurrentRequestsIsolated() {
        // 准备参数：同一个（缓存的）TongYiChatModel 上的两次请求
        Prompt prompt1 = new Prompt(new UserMessage("问题一"));
        Prompt prompt2 = new Prompt(new UserMessage("问题二"));
        // mock 方法：两次请求的 id 相同，最容易串
        Sinks.Many<ChatResponse> sink1 = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.Many<ChatResponse> sink2 = Sinks.many().unicast().onBackpressureBuffer();
        when(openAiChatModel.stream(same(prompt1))).thenReturn(sink1.asFlux());
        when(openAiChatModel.stream(same(prompt2))).thenReturn(sink2.asFlux());

        // 调用：两次请求同时订阅，chunk 交错到达
        List<String> deltas1 = new ArrayList<>();
        List<String> deltas2 = new ArrayList<>();
        chatModel.stream(prompt1).subscribe(response -> deltas1.add(getReasoningContentDelta(response)));
        chatModel.stream(prompt2).subscribe(response -> deltas2.add(getReasoningContentDelta(response)));
        sink1.tryEmitNext(buildChunk("c1", "", "A"));
        sink2.tryEmitNext(buildChunk("c1", "", "X"));
        sink1.tryEmitNext(buildChunk("c1", "", "AB"));
        sink2.tryEmitNext(buildChunk("c1", "", "XY"));
        sink2.tryEmitNext(buildChunk("c1", "", "XYZ"));
        sink1.tryEmitNext(buildChunk("c1", "", "ABC"));
        // 断言：各自计算增量，互不影响
        assertEquals(List.of("A", "B", "C"), deltas1);
        assertEquals(List.of("X", "Y", "Z"), deltas2);
    }

    @Test
    public void testStream_emptyReasoningContent() {
        // 准备参数
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        ChatResponse noReasoningChunk = new ChatResponse(List.of(new Generation(AssistantMessage.builder()
                .content("2").properties(Map.of("id", "c1", "index", 0)).build())));
        // mock 方法：空思考、没有思考两种 chunk
        when(openAiChatModel.stream(same(prompt))).thenReturn(Flux.just(buildChunk("c1", "", ""), noReasoningChunk));

        // 调用
        List<ChatResponse> responses = chatModel.stream(prompt).collectList().block();
        // 断言：空思考的增量为空串
        assertEquals("", getReasoningContentDelta(responses.get(0)));
        assertEquals("", AiChatUtils.getChatResponseReasoningContent(responses.get(0)));
        // 断言：没有思考的 chunk 原样返回
        assertSame(noReasoningChunk, responses.get(1));
        assertNull(AiChatUtils.getChatResponseReasoningContent(responses.get(1)));
    }

    @Test
    public void testCall() {
        // 准备参数
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        ChatResponse response = buildChunk("c1", "2", "完整思考");
        // mock 方法
        when(openAiChatModel.call(same(prompt))).thenReturn(response);

        // 调用
        ChatResponse result = chatModel.call(prompt);
        // 断言：同步原样返回，思考内容取完整值
        assertSame(response, result);
        assertFalse(getMetadata(result).containsKey(OpenAiDelegateChatModel.REASONING_CONTENT_DELTA));
        assertEquals("完整思考", AiChatUtils.getChatResponseReasoningContent(result));
    }

    @Test
    public void testStream_getChatResponseReasoningContent() {
        // 准备参数
        Prompt prompt = new Prompt(new UserMessage("1 + 1 = ？"));
        // mock 方法
        when(openAiChatModel.stream(same(prompt))).thenReturn(Flux.just(
                buildChunk("c1", "", "这是思考内容"), buildChunk("c1", "", "这是思考内容第二段思考"),
                buildChunk("c1", "2", "这是思考内容第二段思考")));

        // 调用：按业务方式逐 chunk 拼接
        String reasoningContent = String.join("", chatModel.stream(prompt)
                .map(AiChatUtils::getChatResponseReasoningContent).collectList().block());
        // 断言：拼接结果不重复
        assertEquals("这是思考内容第二段思考", reasoningContent);
    }

    // ========== 构造 chunk ==========

    /**
     * 构造 OpenAiChatModel 的流式 chunk：metadata 与其一致，含 id、index 与累计的 reasoningContent
     */
    private static ChatResponse buildChunk(String id, String content, String reasoningContent) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id);
        metadata.put("index", 0);
        metadata.put(OpenAiDelegateChatModel.REASONING_CONTENT, reasoningContent);
        return new ChatResponse(List.of(new Generation(AssistantMessage.builder()
                .content(content).properties(metadata).build())));
    }

    private static Map<String, Object> getMetadata(ChatResponse response) {
        return response.getResult().getOutput().getMetadata();
    }

    private static String getReasoningContentDelta(ChatResponse response) {
        return (String) getMetadata(response).get(OpenAiDelegateChatModel.REASONING_CONTENT_DELTA);
    }

    private static List<String> getReasoningContentDeltas(List<ChatResponse> responses) {
        return responses.stream().map(TongYiChatModelTest::getReasoningContentDelta).toList();
    }

}

package cn.iocoder.yudao.module.ai1.harness.llm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AI1 LLM 会话工具
 *
 * 只负责「按给定模型与消息执行一次流式会话」：接收已装配好的系统指令、历史消息、工具与 Advisor，
 * 经 ChatClient 流式会话，增量通过回调输出；传输方式（SSE、Redis Stream）由调用方决定
 *
 * @author 芋道源码
 */
@Component
public class Ai1LlmChatTool {

    /**
     * 思考过程的元数据键（Spring AI OpenAI 推理模型的 reasoning_content）
     */
    private static final String METADATA_REASONING = "reasoningContent";

    @Resource
    private Ai1LlmModelFactory llmModelFactory;

    /**
     * 流式会话：装配消息 → 附加工具、Advisor → 增量输出
     *
     * @param model        模型运行时快照（需为对话模型）
     * @param systemPrompt   系统指令，可为空
     * @param histories      历史消息，按时间升序；每项为 [role, content]
     * @param content        当前用户消息
     * @param tools          工具（ToolCallback 或 @Tool 对象），可为空
     * @param advisors       会话 Advisor，可为空
     * @param sessionId 会话编号，用于替换请求 Header 中的 {session} 占位符
     * @param onThinking     思考过程增量回调，可为空
     * @param onContent      回复内容增量回调，可为空
     * @return 完整回复（内容 + 思考过程）
     */
    public ChatText chat(Ai1ModelRespBO model, String systemPrompt, List<String[]> histories, String content,
                         List<Object> tools, List<Advisor> advisors, Long sessionId,
                         Consumer<String> onThinking, Consumer<String> onContent) {
        // 1. 装配会话请求：消息（系统指令 + 历史 + 当前）+ 工具 + Advisor
        ChatClient.ChatClientRequestSpec spec = llmModelFactory.buildChatClient(model, sessionId).prompt()
                .messages(buildMessages(systemPrompt, histories, content));
        if (CollUtil.isNotEmpty(tools)) {
            spec = spec.tools(tools.toArray());
        }
        if (CollUtil.isNotEmpty(advisors)) {
            spec = spec.advisors(advisors);
        }

        // 2. 流式执行，逐段回调
        StringBuilder contentText = new StringBuilder();
        StringBuilder thinkingText = new StringBuilder();
        String previousReasoning = "";
        for (ChatResponse chatResponse : spec.stream().chatResponse().toIterable()) {
            Generation generation = chatResponse.getResult();
            // 部分供应商在流末尾下发只含 usage、没有输出的分片，跳过避免 NPE
            if (generation == null || generation.getOutput() == null) {
                continue;
            }
            // 2.1 思考过程：推理模型的 reasoning_content 按流累积下发，取变化增量转发，重复值去重
            Object reasoningValue = generation.getOutput().getMetadata().get(METADATA_REASONING);
            String reasoning = reasoningValue instanceof String value ? value : null;
            String delta = computeReasoningDelta(previousReasoning, reasoning);
            if (StrUtil.isNotBlank(delta)) {
                previousReasoning = reasoning;
                thinkingText.append(delta);
                if (onThinking != null) {
                    onThinking.accept(delta);
                }
            }
            // 2.2 回复内容
            String chunk = generation.getOutput().getText();
            if (StrUtil.isNotBlank(chunk)) {
                contentText.append(chunk);
                if (onContent != null) {
                    onContent.accept(chunk);
                }
            }
        }
        return new ChatText(contentText.toString(), thinkingText.toString());
    }

    /**
     * 消息装配：系统指令 + 历史 + 当前用户消息；推理模型的思考过程不进入上下文
     */
    private static List<Message> buildMessages(String systemPrompt, List<String[]> histories, String content) {
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemPrompt)) {
            messages.add(new SystemMessage(systemPrompt));
        }
        if (CollUtil.isNotEmpty(histories)) {
            for (String[] history : histories) {
                String text = StrUtil.nullToEmpty(history[1]);
                if (Ai1SessionMessageRoleEnum.isUser(history[0])) {
                    messages.add(new UserMessage(text));
                } else if (Ai1SessionMessageRoleEnum.isAssistant(history[0])) {
                    messages.add(new AssistantMessage(text));
                }
            }
        }
        messages.add(new UserMessage(content));
        return messages;
    }

    /**
     * 推理内容增量计算：与上轮累积内容比较，取新增部分；无变化返回 null
     */
    private static String computeReasoningDelta(String previousReasoning, String reasoning) {
        if (StrUtil.isBlank(reasoning) || reasoning.equals(previousReasoning)) {
            return null;
        }
        return reasoning.startsWith(previousReasoning) ? reasoning.substring(previousReasoning.length()) : reasoning;
    }

    /**
     * LLM 会话结果
     */
    @Data
    @AllArgsConstructor
    public static class ChatText {

        /**
         * 回复内容
         */
        private String content;
        /**
         * 思考过程，推理模型才有
         */
        private String thinking;

    }

}

package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.chat.ChatClient;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.StreamingChatClient;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.exception.AiException;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * yudao ai client
 *
 * @author fansili
 * @time 2024/4/14 10:27
 * @since 1.0
 */
public class YudaoAiClient implements AiClient {

    protected Map<String, Object> chatClientMap;

    public YudaoAiClient(Map<String, Object> chatClientMap) {
        this.chatClientMap = chatClientMap;
    }

    @Override
    public ChatResponse call(Prompt prompt, String clientName) {
        if (!chatClientMap.containsKey(clientName)) {
            throw new AiException("clientName不存在!");
        }
        ChatClient chatClient = (ChatClient) chatClientMap.get(clientName);
        return chatClient.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt, String clientName) {
        if (!chatClientMap.containsKey(clientName)) {
            throw new AiException("clientName不存在!");
        }
        StreamingChatClient streamingChatClient = (StreamingChatClient) chatClientMap.get(clientName);
        return streamingChatClient.stream(prompt);
    }
}

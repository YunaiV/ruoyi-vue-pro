package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * ai client传入
 *
 * @author fansili
 * @time 2024/4/14 10:27
 * @since 1.0
 */
public interface AiClient {

    ChatResponse call(Prompt prompt, String clientName);

    Flux<ChatResponse> stream(Prompt prompt, String clientName);
}

package org.springframework.ai.model.tool;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.util.Assert;

import java.util.function.BiPredicate;

/**
 * TODO 芋艿：spring-ai-alibaba 2.0.0-M1.1 仍依赖旧的 Spring AI ToolExecutionEligibilityPredicate，
 * 临时补齐；升级到兼容 Spring AI 2.0.0 的 spring-ai-alibaba 版本后，删除本包下的 shim。
 */
public interface ToolExecutionEligibilityPredicate extends BiPredicate<ChatOptions, ChatResponse> {

    default boolean isToolExecutionRequired(ChatOptions promptOptions, ChatResponse chatResponse) {
        Assert.notNull(promptOptions, "promptOptions cannot be null");
        Assert.notNull(chatResponse, "chatResponse cannot be null");
        return test(promptOptions, chatResponse);
    }

}

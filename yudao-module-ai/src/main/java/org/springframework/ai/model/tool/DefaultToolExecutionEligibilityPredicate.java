package org.springframework.ai.model.tool;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.lang.reflect.Method;

/**
 * TODO 芋艿：spring-ai-alibaba 2.0.0-M1.1 仍依赖旧的 Spring AI ToolExecutionEligibilityPredicate，
 * 临时补齐；升级到兼容 Spring AI 2.0.0 的 spring-ai-alibaba 版本后，删除本包下的 shim。
 */
public class DefaultToolExecutionEligibilityPredicate implements ToolExecutionEligibilityPredicate {

    @Override
    public boolean test(ChatOptions promptOptions, ChatResponse chatResponse) {
        return isInternalToolExecutionEnabled(promptOptions) && chatResponse != null && chatResponse.hasToolCalls();
    }

    private static boolean isInternalToolExecutionEnabled(ChatOptions promptOptions) {
        try {
            Method method = promptOptions.getClass().getMethod("getInternalToolExecutionEnabled");
            Object result = method.invoke(promptOptions);
            return result == null || Boolean.TRUE.equals(result);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return true;
        }
    }

}

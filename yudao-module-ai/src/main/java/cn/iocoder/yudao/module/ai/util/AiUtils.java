package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springaicommunity.moonshot.MoonshotChatOptions;
import org.springaicommunity.qianfan.QianFanChatOptions;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;

import java.util.*;

/**
 * Spring AI 工具类
 *
 * @author 芋道源码
 */
public class AiUtils {

    public static final String TOOL_CONTEXT_LOGIN_USER = "LOGIN_USER";
    public static final String TOOL_CONTEXT_TENANT_ID = "TENANT_ID";

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        return buildChatOptions(platform, model, temperature, maxTokens, null, null);
    }

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens,
                                               List<ToolCallback> toolCallbacks, Map<String, Object> toolContext) {
        toolCallbacks = ObjUtil.defaultIfNull(toolCallbacks, Collections.emptyList());
        toolContext = ObjUtil.defaultIfNull(toolContext, Collections.emptyMap());
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return DashScopeChatOptions.builder().withModel(model).withTemperature(temperature).withMaxToken(maxTokens)
                        .withEnableThinking(true) // TODO 芋艿：默认都开启 thinking 模式，后续可以让用户配置
                        .withToolCallbacks(toolCallbacks).withToolContext(toolContext).build();
            case YI_YAN:
                return QianFanChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case DEEP_SEEK:
            case DOU_BAO: // 复用 DeepSeek 客户端
            case HUN_YUAN: // 复用 DeepSeek 客户端
            case SILICON_FLOW: // 复用 DeepSeek 客户端
            case XING_HUO: // 复用 DeepSeek 客户端
                return DeepSeekChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case ZHI_PU:
                return ZhiPuAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case MINI_MAX:
                return MiniMaxChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case MOONSHOT:
                return MoonshotChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OPENAI:
            case GEMINI: // 复用 OpenAI 客户端
            case BAI_CHUAN: // 复用 OpenAI 客户端
                return OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case AZURE_OPENAI:
                return AzureOpenAiChatOptions.builder().deploymentName(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case ANTHROPIC:
                return AnthropicChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OLLAMA:
                return OllamaOptions.builder().model(model).temperature(temperature).numPredict(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    public static Message buildMessage(String type, String content) {
        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(content);
        }
        if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(content);
        }
        if (MessageType.SYSTEM.getValue().equals(type)) {
            return new SystemMessage(content);
        }
        if (MessageType.TOOL.getValue().equals(type)) {
            throw new UnsupportedOperationException("暂不支持 tool 消息：" + content);
        }
        throw new IllegalArgumentException(StrUtil.format("未知消息类型({})", type));
    }

    public static Map<String, Object> buildCommonToolContext() {
        Map<String, Object> context = new HashMap<>();
        context.put(TOOL_CONTEXT_LOGIN_USER, SecurityFrameworkUtils.getLoginUser());
        context.put(TOOL_CONTEXT_TENANT_ID, TenantContextHolder.getTenantId());
        return context;
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseReasoningContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        if (response.getResult().getOutput() instanceof DeepSeekAssistantMessage) {
            return ((DeepSeekAssistantMessage) (response.getResult().getOutput())).getReasoningContent();
        }
        return null;
    }

}
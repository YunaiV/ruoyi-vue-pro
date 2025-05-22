package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.moonshot.MoonshotChatOptions;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.qianfan.QianFanChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
                                               Set<String> toolNames, Map<String, Object> toolContext) {
        toolNames = ObjUtil.defaultIfNull(toolNames, Collections.emptySet());
        toolContext = ObjUtil.defaultIfNull(toolContext, Collections.emptyMap());
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return DashScopeChatOptions.builder().withModel(model).withTemperature(temperature).withMaxToken(maxTokens)
                        .withFunctions(toolNames).withToolContext(toolContext).build();
            case YI_YAN:
                return QianFanChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case ZHI_PU:
                return ZhiPuAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .functions(toolNames).toolContext(toolContext).build();
            case MINI_MAX:
                return MiniMaxChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .functions(toolNames).toolContext(toolContext).build();
            case MOONSHOT:
                return MoonshotChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .functions(toolNames).toolContext(toolContext).build();
            case OPENAI:
            case DEEP_SEEK: // 复用 OpenAI 客户端
            case DOU_BAO: // 复用 OpenAI 客户端
            case HUN_YUAN: // 复用 OpenAI 客户端
            case XING_HUO: // 复用 OpenAI 客户端
            case SILICON_FLOW: // 复用 OpenAI 客户端
            case BAI_CHUAN: // 复用 OpenAI 客户端
                return OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolNames(toolNames).toolContext(toolContext).build();
            case AZURE_OPENAI:
                return AzureOpenAiChatOptions.builder().deploymentName(model).temperature(temperature).maxTokens(maxTokens)
                        .toolNames(toolNames).toolContext(toolContext).build();
            case OLLAMA:
                return OllamaOptions.builder().model(model).temperature(temperature).numPredict(maxTokens)
                        .toolNames(toolNames).toolContext(toolContext).build();
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

}
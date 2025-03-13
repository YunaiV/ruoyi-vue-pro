package cn.iocoder.yudao.framework.ai.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
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

/**
 * Spring AI 工具类
 *
 * @author 芋道源码
 */
public class AiUtils {

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                // TODO functions
                return DashScopeChatOptions.builder().withModel(model).withTemperature(temperature).withMaxToken(maxTokens).build();
            case YI_YAN:
                return QianFanChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case ZHI_PU:
                // TODO functions
                return ZhiPuAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case MINI_MAX:
                // TODO functions
                return MiniMaxChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case MOONSHOT:
                // TODO functions
                return MoonshotChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens).build();
            case OPENAI:
            case DEEP_SEEK: // 复用 OpenAI 客户端
            case DOU_BAO: // 复用 OpenAI 客户端
            case HUN_YUAN: // 复用 OpenAI 客户端
            case XING_HUO: // 复用 OpenAI 客户端
            case SILICON_FLOW: // 复用 OpenAI 客户端
                return OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
//                        .toolNames() TODO
                        .toolNames("listDir")
                        .build();
            case AZURE_OPENAI:
                // TODO 芋艿：貌似没 model 字段？？？！
                // TODO 芋艿：.toolNames() TODO
                return AzureOpenAiChatOptions.builder().deploymentName(model).temperature(temperature).maxTokens(maxTokens).build();
            case OLLAMA:
                // TODO 芋艿：.toolNames() TODO
                return OllamaOptions.builder().model(model).temperature(temperature).numPredict(maxTokens)
                        .toolNames("listDir").build();
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

}
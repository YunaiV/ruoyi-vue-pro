package cn.iocoder.yudao.framework.ai.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.deepseek.DeepSeekChatOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatOptions;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.ChatOptions;
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
        Float temperatureF = temperature != null ? temperature.floatValue() : null;
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return TongYiChatOptions.builder().withModel(model).withTemperature(temperature).withMaxTokens(maxTokens).build();
            case YI_YAN:
                return QianFanChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case DEEP_SEEK:
                return DeepSeekChatOptions.builder().model(model).temperature(temperatureF).maxTokens(maxTokens).build();
            case ZHI_PU:
                return ZhiPuAiChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case XING_HUO:
                return XingHuoChatOptions.builder().model(model).temperature(temperatureF).maxTokens(maxTokens).build();
            case OPENAI:
                return OpenAiChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case AZURE_OPENAI:
                // TODO 芋艿：貌似没 model 字段？？？！
                return AzureOpenAiChatOptions.builder().withDeploymentName(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case OLLAMA:
                return OllamaOptions.create().withModel(model).withTemperature(temperatureF).withNumPredict(maxTokens);
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
        if (MessageType.FUNCTION.getValue().equals(type)) {
            return new FunctionMessage(content);
        }
        throw new IllegalArgumentException(StrUtil.format("未知消息类型({})", type));
    }

}
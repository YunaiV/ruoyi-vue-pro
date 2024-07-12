package cn.iocoder.yudao.framework.ai.core.model.deepseek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * DeepSeek {@link ChatOptions} 实现类
 *
 * 参考文档：<a href="https://platform.deepseek.com/api-docs/zh-cn/">快速开始</a>
 *
 * @author fansili
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeepSeekChatOptions implements ChatOptions {

    public static final String MODEL_DEFAULT = "deepseek-chat";

    /**
     * 模型
     */
    private String model;
    /**
     * 温度
     */
    private Float temperature;
    /**
     * 最大 Token
     */
    private Integer maxTokens;
    /**
     * topP
     */
    private Float topP;

    @Override
    public Integer getTopK() {
        return null;
    }

    public static DeepSeekChatOptions fromOptions(DeepSeekChatOptions fromOptions) {
        return DeepSeekChatOptions.builder()
                .model(fromOptions.getModel())
                .temperature(fromOptions.getTemperature())
                .maxTokens(fromOptions.getMaxTokens())
                .topP(fromOptions.getTopP())
                .build();
    }

}

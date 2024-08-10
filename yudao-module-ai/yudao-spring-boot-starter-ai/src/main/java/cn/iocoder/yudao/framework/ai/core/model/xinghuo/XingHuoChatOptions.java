package cn.iocoder.yudao.framework.ai.core.model.xinghuo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * 讯飞星火 {@link ChatOptions} 实现类
 *
 * 参考文档：<a href="https://www.xfyun.cn/doc/spark/HTTP%E8%B0%83%E7%94%A8%E6%96%87%E6%A1%A3.html">HTTP 调用</a>
 *
 * @author fansili
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XingHuoChatOptions implements ChatOptions {

    public static final String MODEL_DEFAULT = "generalv3.5";

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
     * K 个候选
     */
    private Integer topK;

    @Override
    public Float getTopP() {
        return null;
    }

    public static XingHuoChatOptions fromOptions(XingHuoChatOptions fromOptions) {
        return XingHuoChatOptions.builder()
                .model(fromOptions.getModel())
                .temperature(fromOptions.getTemperature())
                .maxTokens(fromOptions.getMaxTokens())
                .topK(fromOptions.getTopK())
                .build();
    }

}

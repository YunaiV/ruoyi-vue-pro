package cn.iocoder.yudao.module.ai.dal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat config
 *
 * @author fansili
 * @time 2024/5/6 15:06
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatModalChatConfigVO extends AiChatModalConfigVO {

    @NotNull
    @Schema(description = "在生成消息时采用的Top-K采样大小")
    private Double topK;

    @NotNull
    @Schema(description = "Top-P核采样方法的概率阈值")
    private Double topP;

    @NotNull
    @Schema(description = "温度参数，用于调整生成回复的随机性和多样性程度")
    private Double temperature;

    @NotNull
    @Schema(description = "最大 tokens")
    private Integer maxTokens;

}

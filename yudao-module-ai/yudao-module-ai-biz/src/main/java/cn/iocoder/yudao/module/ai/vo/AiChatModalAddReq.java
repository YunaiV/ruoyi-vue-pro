package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ai chat modal
 *
 * @author fansili
 * @time 2024/4/24 19:47
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatModalAddReq {

    @NotNull
    @Schema(description = "模型名字")
    @Size(max = 60, message = "模型名字最大60")
    private String modelName;

    @NotNull
    @Schema(description = "模型类型(qianwen、yiyan、xinghuo、openai)")
    @Size(max = 32, message = "模型类型最大32")
    private String modelType;

    @Schema(description = "模型照片")
    private String modalImage;

    @Schema(description = "模型配置JSON")
    private String modelConfig;

}

package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

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
    @Size(max = 60, message = "模型名字最大60个字符")
    private String name;

    @NotNull
    @Size(max = 32, message = "模型平台最大32个字符")
    @Schema(description = "模型平台 参考 AiPlatformEnum")
    private String platform;

    @NotNull
    @Schema(description = "模型类型(qianwen、yiyan、xinghuo、openai)")
    @Size(max = 32, message = "模型类型最大32个字符")
    private String modal;

    @Schema(description = "模型照片")
    @Size(max = 256, message = "模型照片地址最大256个字符")
    private String imageUrl;

    @Schema(description = "模型配置JSON")
//    @Size(max = 1024, message = "模型配置最大1024个字符")
    private Map<String, Object> config;
}

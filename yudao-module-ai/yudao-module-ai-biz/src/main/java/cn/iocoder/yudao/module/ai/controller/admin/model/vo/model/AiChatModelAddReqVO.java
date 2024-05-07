package cn.iocoder.yudao.module.ai.controller.admin.model.vo.model;

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
public class AiChatModelAddReqVO {

    @Schema(description = "API 秘钥编号")
    @NotNull(message = "API 秘钥编号不能为空!")
    private Long keyId;

    @Schema(description = "模型名字")
    @Size(max = 60, message = "模型名字最大60个字符")
    @NotNull(message = "模型名字不能为空!")
    private String name;

    @Schema(description = "模型类型(qianwen、yiyan、xinghuo、openai)")
    @Size(max = 32, message = "模型类型最大32个字符")
    @NotNull(message = "model模型不能为空!")
    private String model;

    @Size(max = 32, message = "模型平台最大32个字符")
    @Schema(description = "模型平台 参考 AiPlatformEnum")
    @NotNull(message = "平台不能为空!")
    private String platform;

    @Schema(description = "排序")
    @NotNull(message = "sort排序不能为空!")
    private Integer sort;

    // ========== 会话配置 ==========

    @Schema(description = "温度参数")
    private Integer temperature;

    @Schema(description = "单条回复的最大 Token 数量")
    private Integer maxTokens;

    @Schema(description = "上下文的最大 Message 数量")
    private Integer maxContexts;
}

package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理后台 - AI 服装设计智能对话 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装设计智能对话 Request VO")
@Data
public class AiFashionSmartChatReqVO {

    @Schema(description = "会话令牌，首次为空，后续传入以维持上下文", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionToken;

    @Schema(description = "用户消息（可以是一个字、短语或完整句子）", requiredMode = Schema.RequiredMode.REQUIRED, example = "出5款甜酷风连衣裙")
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "质量预设覆盖（不填使用自动推断）", example = "HIGH")
    private String qualityPreset;

    @Schema(description = "生成宽度（像素）", example = "768")
    private Integer width = 768;

    @Schema(description = "生成高度（像素）", example = "1024")
    private Integer height = 1024;

    @Schema(description = "工作流模式覆盖（不填使用自动推断）", example = "PROFESSIONAL")
    private String workflowModeOverride;

}

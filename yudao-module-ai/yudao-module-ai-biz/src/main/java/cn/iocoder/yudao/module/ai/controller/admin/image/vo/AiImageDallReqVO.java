package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * dall2/dall2 绘画
 *
 * @author fansili
 * @time 2024/4/25 16:24
 * @since 1.0
 */
@Data
public class AiImageDallReqVO {

    @Schema(description = "模型平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String platform; // 参见 AiPlatformEnum 枚举

    @Schema(description = "提示词")
    @NotNull(message = "提示词不能为空!")
    @Size(max = 1200, message = "提示词最大1200")
    private String prompt;

    @Schema(description = "模型(dall2、dall3)")
    @NotNull(message = "模型不能为空")
    private String model;

    @Schema(description = "图像生成的风格。可为vivid（生动）或natural（自然)")
    private String style;

    @Schema(description = "图片高度。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。")
    @NotNull(message = "图片高度不能为空!")
    private Integer height;

    @Schema(description = "图片宽度。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。")
    @NotNull(message = "图片宽度不能为空!")
    private Integer width;

}

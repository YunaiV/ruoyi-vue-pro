package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * dall2/dall2 绘画
 *
 * @author fansili
 * @time 2024/4/25 16:24
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiImageDallRespVO {

    @Schema(description = "提示词")
    @NotNull(message = "提示词不能为空!")
    @Size(max = 1200, message = "提示词最大1200")
    private String prompt;

    @Schema(description = "模型")
    @NotNull(message = "模型不能为空")
    private String model;

    @Schema(description = "风格")
    private String style;

    @Schema(description = "图片size 1024x1024 ...")
    private String size;

    @Schema(description = "图片地址(自己服务器)")
    private String picUrl;

    @Schema(description = "可以访问图像的URL。")
    private String originalPicUrl;

    @Schema(description = "图片base64。")
    private String base64;

    @Schema(description = "错误信息。")
    private String errorMessage;

}

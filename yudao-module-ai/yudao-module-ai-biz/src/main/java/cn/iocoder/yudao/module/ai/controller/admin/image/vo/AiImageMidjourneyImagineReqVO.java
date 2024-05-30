package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

// TODO @fan：待定
/**
 * midjourney req
 *
 * @author fansili
 * @time 2024/4/28 17:42
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiImageMidjourneyImagineReqVO {

    @Schema(description = "提示词")
    @NotNull(message = "提示词不能为空!")
    private String prompt;

    @Schema(description = "模型(midjourney、niji)")
    private String model;

    @Schema(description = "垫图(参考图)base64数组")
    private List<String> base64Array;
}

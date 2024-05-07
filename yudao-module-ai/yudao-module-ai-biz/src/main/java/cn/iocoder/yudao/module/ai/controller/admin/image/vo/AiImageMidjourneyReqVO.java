package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * midjourney req
 *
 * @author fansili
 * @time 2024/4/28 17:42
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiImageMidjourneyReqVO {

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "绘画比例 1:1、3:4、4:3、9:16、16:9")
    private String size;

    @Schema(description = "风格")
    private String style;

    @Schema(description = "参考图")
    private String referImage;
}

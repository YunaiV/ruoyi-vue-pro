package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
public class AiImageDallDrawingReq {

    @Schema(description = "提示词")
    @NotNull(message = "提示词不能为空!")
    private String prompt;
}

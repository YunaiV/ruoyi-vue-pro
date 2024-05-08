package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
public class AiImageMidjourneyOperateReqVO {


    @NotNull(message = "图片编号不能为空")
    @Schema(description = "编号")
    private String id;

    @NotNull(message = "消息编号不能为空")
    @Schema(description = "消息编号")
    private String messageId;

    @NotNull(message = "操作编号不能为空")
    @Schema(description = "操作编号")
    private String operateId;
}

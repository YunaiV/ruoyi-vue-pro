package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Midjourney：action 请求
 *
 * @author fansili
 * @time 2024/5/30 14:02
 * @since 1.0
 */
@Data
public class MidjourneyActionReqVO {

    @Schema(description = "操作按钮id", required = true)
    @NotNull(message = "customId 不能为空!")
    private String customId;

    @Schema(description = "操作按钮id", required = true)
    @NotNull(message = "customId 不能为空!")
    private String taskId;

    @Schema(description = "通知地址", required = false)
    @NotNull(message = "回调地址不能为空!")
    private String notifyHook;

    @Schema(description = "自定义参数", required = false)
    private String state;
}

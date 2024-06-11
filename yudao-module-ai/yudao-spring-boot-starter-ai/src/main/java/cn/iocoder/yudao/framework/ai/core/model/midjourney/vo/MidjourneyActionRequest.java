package cn.iocoder.yudao.framework.ai.core.model.midjourney.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Midjourney：action 请求
 *
 * @author fansili
 * @time 2024/5/30 14:02
 * @since 1.0
 */
@Data
public class MidjourneyActionRequest {

    @Schema(description = "操作按钮id", required = true)
    private String customId;

    @Schema(description = "操作按钮id", required = true)
    private String taskId;

    @Schema(description = "通知地址", required = false)
    private String notifyHook;

    @Schema(description = "自定义参数", required = false)
    private String state;
}

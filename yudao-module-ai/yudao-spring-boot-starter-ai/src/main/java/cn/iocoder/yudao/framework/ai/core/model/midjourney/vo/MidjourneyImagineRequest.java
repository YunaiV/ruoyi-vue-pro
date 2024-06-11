package cn.iocoder.yudao.framework.ai.core.model.midjourney.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// TODO @fan：待定
/**
 * Midjourney：Imagine 请求
 *
 * @author fansili
 * @time 2024/5/30 14:02
 * @since 1.0
 */
@Data
public class MidjourneyImagineRequest {

    @Schema(description = "垫图(参考图)base64数组", required = false)
    private List<String> base64Array;

    @Schema(description = "通知地址", required = false)
    private String notifyHook;

    @Schema(description = "提示词", required = true)
    private String prompt;

    @Schema(description = "自定义参数", required = false)
    private String state;
}

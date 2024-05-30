package cn.iocoder.yudao.module.ai.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * Midjourney：Imagine 请求
 *
 * @author fansili
 * @time 2024/5/30 14:02
 * @since 1.0
 */
@Data
public class MidjourneySubmitRespVO {

    @Schema(description = "状态码: 1(提交成功), 21(已存在), 22(排队中), other(错误)")
    private String code;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "扩展字段")
    private Map<String, Object> properties;

    @Schema(description = "任务ID")
    private String result;
}

package cn.iocoder.yudao.module.infra.controller.admin.sse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - SSE 配置 Response VO")
@Data
public class SseConfigRespVO {

    @Schema(description = "SSE 是否开启", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean sseEnabled;

    @Schema(description = "SSE 接口路径前缀", example = "/infra/sse")
    private String ssePath;

    @Schema(description = "SSE 心跳间隔，单位：毫秒", example = "25000")
    private Long sseHeartbeatInterval;

    @Schema(description = "SSE 重连间隔，单位：毫秒", example = "5000")
    private Long sseRetryInterval;

    @Schema(description = "WebSocket 是否开启，通知推送弹窗选择推送方式使用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean websocketEnabled;

}

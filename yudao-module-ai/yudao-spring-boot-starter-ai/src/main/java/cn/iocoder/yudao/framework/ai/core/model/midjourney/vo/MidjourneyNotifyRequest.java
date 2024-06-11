package cn.iocoder.yudao.framework.ai.core.model.midjourney.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Midjourney Proxy 通知回调
 *
 *  - Midjourney Proxy：通知回调 bean 是 com.github.novicezk.midjourney.support.Task
 *  - 毫秒 api 通知回调文档地址：https://gpt-best.apifox.cn/doc-3530863
 *
 * @author fansili
 * @time 2024/5/31 10:37
 * @since 1.0
 */
@Data
public class MidjourneyNotifyRequest {

    @Schema(description = "job id")
    private String id;

    @Schema(description = "任务类型 MidjourneyTaskActionEnum")
    private String action;
    @Schema(description = "任务状态 MidjourneyTaskStatusEnum")
    private String status;

    @Schema(description = "提示词")
    private String prompt;
    @Schema(description = "提示词-英文")
    private String promptEn;

    @Schema(description = "任务描述")
    private String description;
    @Schema(description = "自定义参数")
    private String state;

    @Schema(description = "提交时间")
    private Long submitTime;
    @Schema(description = "开始执行时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long finishTime;

    @Schema(description = "图片url")
    private String imageUrl;

    @Schema(description = "任务进度")
    private String progress;
    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "任务完成后的可执行按钮")
    private List<Button> buttons;

    @Data
    public static class Button {

        @Schema(description = "MJ::JOB::upsample::1::85a4b4c1-8835-46c5-a15c-aea34fad1862 动作标识")
        private String customId;

        @Schema(description = "图标 emoji")
        private String emoji;

        @Schema(description = "Make Variations 文本")
        private String label;

        @Schema(description = "类型，系统内部使用")
        private String type;

        @Schema(description = "样式: 2（Primary）、3（Green）")
        private String style;
    }
}

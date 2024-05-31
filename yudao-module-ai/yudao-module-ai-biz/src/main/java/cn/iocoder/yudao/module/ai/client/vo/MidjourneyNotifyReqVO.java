package cn.iocoder.yudao.module.ai.client.vo;

import cn.iocoder.yudao.module.ai.client.enums.MidjourneyTaskActionEnum;
import cn.iocoder.yudao.module.ai.client.enums.MidjourneyTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author fansili
 * @time 2024/5/31 10:37
 * @since 1.0
 */
@Data
public class MidjourneyNotifyReqVO {

    @Schema(description = "job id")
    private String id;

    @Schema(description = "任务类型")
    private MidjourneyTaskActionEnum action;
    @Schema(description = "任务状态")
    private MidjourneyTaskStatusEnum status = MidjourneyTaskStatusEnum.NOT_START;

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

}

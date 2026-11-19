package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 短剧生成任务 Response VO：由角色形象、场景、视觉资产、分镜、成片记录汇总而来")
@Data
public class AiDramaTaskRespVO {

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer type;

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "项目名称", example = "末班地铁")
    private String projectName;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋艿")
    private String userNickname;

    @Schema(description = "任务标题", example = "林夏 · 形象 1")
    private String title;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer status;

    @Schema(description = "失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "提示词", example = "短发女孩")
    private String prompt;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "上游任务编号", example = "cgt-xxx")
    private String taskId;

    @Schema(description = "结果地址", example = "https://www.iocoder.cn/1.mp4")
    private String resultUrl;

    @Schema(description = "是否已超时：进行中且超过 60 分钟未更新", example = "false")
    private Boolean timeout;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}

package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaVideoSubmissionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 短剧镜头视频提交记录 Response VO")
@Data
public class AiDramaVideoSubmissionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "请求编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6f1c2d3e4b5a69788796a5b4c3d2e1f0")
    private String requestId;

    @Schema(description = "分镜编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long storyboardId;

    @Schema(description = "参数哈希", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parametersHash;

    @Schema(description = "视频模型编号", example = "1")
    private Long modelId;

    @Schema(description = "视频模型标识", example = "doubao-seedance-1-0-pro")
    private String model;

    @Schema(description = "上游视频任务编号：为空且待处理时，提交结果未知", example = "cgt-2025")
    private String taskId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "视频地址", example = "https://www.iocoder.cn/1.mp4")
    private String videoUrl;

    @Schema(description = "视频尾帧地址", example = "https://www.iocoder.cn/1.png")
    private String lastFrameUrl;

    @Schema(description = "失败原因")
    private String errorMessage;

    @Schema(description = "提交前的视频")
    private AiDramaVideoSubmissionDO.VideoSnapshot previousVideo;

    @Schema(description = "提交时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

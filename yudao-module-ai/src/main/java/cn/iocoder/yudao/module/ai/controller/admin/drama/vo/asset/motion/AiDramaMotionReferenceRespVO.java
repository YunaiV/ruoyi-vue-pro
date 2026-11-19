package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 短剧动作参考 Response VO")
@Data
public class AiDramaMotionReferenceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long projectId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "转身挥手")
    private String name;

    @Schema(description = "视频地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp4")
    private String fileUrl;

    @Schema(description = "时长（秒）", example = "5.2")
    private Double duration;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

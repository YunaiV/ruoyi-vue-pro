package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧批量生成关键帧 Request VO：当前剧本中镜头序号在范围内、还没有关键帧的镜头")
@Data
public class AiDramaStoryboardFrameBatchReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "绘画模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "绘画模型不能为空")
    private Long modelId;

    @Schema(description = "开始的镜头序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "开始的镜头序号不能为空")
    @Min(value = 1, message = "开始的镜头序号最小为 1")
    private Integer startSceneNo;

    @Schema(description = "结束的镜头序号（包含）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "结束的镜头序号不能为空")
    @Min(value = 1, message = "结束的镜头序号最小为 1")
    private Integer endSceneNo;

}

package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧导入审阅后的分镜 Request VO：替换该剧本的分镜")
@Data
public class AiDramaStoryboardImportReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "规划时的剧本编号：须仍为当前剧本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "剧本编号不能为空")
    private Long scriptId;

    @Schema(description = "分镜：按播放顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分镜不能为空")
    @Size(max = 500, message = "分镜不能超过 500 个")
    @Valid
    private List<AiDramaStoryboardSaveReqVO> storyboards;

}

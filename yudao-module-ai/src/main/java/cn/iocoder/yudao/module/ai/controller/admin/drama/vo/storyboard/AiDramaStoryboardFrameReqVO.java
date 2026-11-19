package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧镜头关键帧生成 Request VO")
@Data
public class AiDramaStoryboardFrameReqVO {

    @Schema(description = "分镜编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "分镜编号不能为空")
    private Long id;

    @Schema(description = "绘画模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "绘画模型不能为空")
    private Long modelId;

    @Schema(description = "构图修正说明：不为空时，以当前关键帧为参考修正构图", example = "人物移到画面左侧，镜头再拉远一些")
    @Size(max = 500, message = "构图修正说明不能超过 500 个字符")
    private String revisePrompt;

}

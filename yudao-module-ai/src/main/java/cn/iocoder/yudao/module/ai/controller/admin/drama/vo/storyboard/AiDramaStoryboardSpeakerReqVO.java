package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧镜头发言角色设置 Request VO")
@Data
public class AiDramaStoryboardSpeakerReqVO {

    @Schema(description = "分镜编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "分镜编号不能为空")
    private Long id;

    @Schema(description = "发言角色编号：按顺序；为空表示按台词自动识别", example = "[1, 2]")
    @Size(max = 10, message = "发言角色不能超过 10 个")
    private List<Long> characterIds;

}

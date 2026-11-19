package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧选用 AI 音乐为配乐 Request VO")
@Data
public class AiDramaAudioMusicReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "AI 音乐编号：需为我的、已生成完成的音乐", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "AI 音乐编号不能为空")
    private Long musicId;

    @Schema(description = "使用的分镜编号：为空表示候选", example = "[1, 2]")
    private List<Long> storyboardIds;

}

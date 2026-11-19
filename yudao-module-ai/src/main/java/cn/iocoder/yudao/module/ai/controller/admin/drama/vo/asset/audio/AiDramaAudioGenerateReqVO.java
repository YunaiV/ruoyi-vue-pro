package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧音频生成 Request VO：使用语音模型合成角色样音或旁白")
@Data
public class AiDramaAudioGenerateReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "角色编号：为空时生成旁白", example = "1")
    private Long characterId;

    @Schema(description = "语音模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "语音模型不能为空")
    private Long modelId;

    @Schema(description = "音色", requiredMode = Schema.RequiredMode.REQUIRED, example = "longxiaochun")
    @NotBlank(message = "音色不能为空")
    @Size(max = 128, message = "音色不能超过 128 个字符")
    private String voice;

    @Schema(description = "文本", requiredMode = Schema.RequiredMode.REQUIRED, example = "这是最后一班车了。")
    @NotBlank(message = "文本不能为空")
    @Size(max = 2000, message = "文本不能超过 2000 个字符")
    private String text;

    @Schema(description = "语速倍数", example = "1.0")
    private Double speed;

}

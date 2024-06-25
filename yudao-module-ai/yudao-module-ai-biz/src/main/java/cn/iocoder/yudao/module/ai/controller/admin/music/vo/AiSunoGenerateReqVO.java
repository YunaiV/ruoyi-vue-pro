package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author xiaoxin
 */
@Schema(description = "管理后台 - 音乐生成 Request VO")
@Data
public class AiSunoGenerateReqVO {

    @Schema(description = "用于生成音乐音频的提示", example = "创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。")
    private String prompt;

    @Schema(description = "是否纯音乐", example = "true")
    private Boolean makeInstrumental;

    @Schema(description = "模型版本, 默认 chirp-v3.5", example = "chirp-v3.5")
    private String modelVersion;// 参见 AiModelEnum 枚举

    @Schema(description = "音乐风格", example = "[\"pop\",\"jazz\",\"punk\"]")
    private List<String> tags;

    @Schema(description = "音乐/歌曲名称", example = "夜空中最亮的星")
    private String title;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "Suno")
    @NotBlank(message = "平台不能为空")
    private String platform;// 参见 AiPlatformEnum 枚举

    @Schema(description = "生成模式 1(歌词模式), 2(描述模式)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotBlank(message = "生成模式不能为空")
    private String generateMode;// 参见 AiMusicGenerateEnum 枚举

}
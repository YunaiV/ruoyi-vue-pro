package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 音乐生成 Request VO")
@Data
public class AiSunoGenerateReqVO {

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "Suno")
    @NotBlank(message = "平台不能为空")
    private String platform; // 参见 AiPlatformEnum 枚举

    /**
     * 1. 描述模式：描述词 + 是否纯音乐 + 模型
     * 2. 歌词模式：歌词 + 音乐风格 + 标题 + 模型
     */
    @Schema(description = "生成模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "生成模式不能为空")
    private Integer generateMode; // 参见 AiMusicGenerateModeEnum 枚举

    // TODO @xin：方案一：prompt => lyric 歌词；gptDescriptionPrompt => description 描述（db 那字段也改下，避免和 gpt 直接耦合）；这样搞完后，会更统一好理解一点
    // TODO @xin：方案二：还是之前的做法，都用 prompt；不过最终 gptDescriptionPrompt 还是存储 description 算描述。可以微信一起讨论下。
    @Schema(description = "用于生成音乐音频的歌词提示",
            example = """
                    [Verse]
                    阳光下奔跑 多么欢快
                    假期就要来 心都飞起来
                    朋友在一旁 笑声又灿烂
                    无忧无虑的 每一天甜蜜
                    [Chorus]
                    马上放假了 快来庆祝
                    一起去旅行 快去冒险
                    日子太短暂 别再等待
                    马上放假了 梦想起飞
                    """)
    private String prompt; // 歌词模式用

    @Schema(description = "用于生成音乐音频的描述",
            example = "创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。")
    private String gptDescriptionPrompt; // 描述模式用

    @Schema(description = "是否纯音乐", example = "true")
    private Boolean makeInstrumental;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "chirp-v3.5")
    @NotEmpty(message = "模型不能为空")
    private String model; // 参见 AiModelEnum 枚举

    @Schema(description = "音乐风格", example = "[\"pop\",\"jazz\",\"punk\"]")
    private List<String> tags;

    @Schema(description = "音乐/歌曲名称", example = "夜空中最亮的星")
    private String title;

}
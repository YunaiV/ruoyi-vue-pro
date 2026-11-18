package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Objects;

// DONE @AI：swagger 里，要写类注释么？（以 @Schema 为准，Javadoc 只保留 @author）
/**
 * AI 音乐创作（灵感 / 自定义） Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐创作（灵感 / 自定义） Request VO")
@Data
public class AiMusicGenerateReqVO {

    @Schema(description = "生成模式：1 灵感模式 2 自定义模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "生成模式不能为空")
    private Integer generateMode;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "歌曲描述（灵感模式）", example = "一首关于毕业旅行的轻快流行歌，吉他为主")
    @Size(max = 1024, message = "歌曲描述不能超过 1024 个字符")
    private String prompt;

    @Schema(description = "歌词（自定义模式）", example = "[Verse]\n晚风吹过街角的路灯")
    @Size(max = 1024, message = "歌词不能超过 1024 个字符")
    private String lyric;

    @Schema(description = "音乐风格，多个用英文逗号分隔", example = "pop,female vocals,summer")
    @Size(max = 120, message = "音乐风格不能超过 120 个字符")
    private String tags;

    @Schema(description = "歌曲名称（自定义模式）", example = "夏夜晚风")
    @Size(max = 100, message = "歌曲名称不能超过 100 个字符")
    private String title;

    @Schema(description = "是否纯音乐", example = "false")
    private Boolean makeInstrumental;

    @AssertTrue(message = "生成模式只能是灵感模式或自定义模式")
    @JsonIgnore
    public boolean isGenerateModeValid() {
        return Objects.equals(generateMode, AiMusicGenerateModeEnum.INSPIRATION.getMode())
                || Objects.equals(generateMode, AiMusicGenerateModeEnum.CUSTOM.getMode());
    }

    @AssertTrue(message = "请输入歌曲描述")
    @JsonIgnore
    public boolean isPromptValid() {
        return !Objects.equals(generateMode, AiMusicGenerateModeEnum.INSPIRATION.getMode())
                || StrUtil.isNotBlank(prompt);
    }

    @AssertTrue(message = "请输入歌词")
    @JsonIgnore
    public boolean isLyricValid() {
        return !Objects.equals(generateMode, AiMusicGenerateModeEnum.CUSTOM.getMode())
                || Boolean.TRUE.equals(makeInstrumental) || StrUtil.isNotBlank(lyric);
    }

    @AssertTrue(message = "请输入歌曲名称")
    @JsonIgnore
    public boolean isTitleValid() {
        return !Objects.equals(generateMode, AiMusicGenerateModeEnum.CUSTOM.getMode())
                || StrUtil.isNotBlank(title);
    }

}

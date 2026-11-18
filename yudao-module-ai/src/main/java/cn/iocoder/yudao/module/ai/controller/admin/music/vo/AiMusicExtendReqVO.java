package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 音乐续写 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐续写 Request VO")
@Data
public class AiMusicExtendReqVO {

    @Schema(description = "来源音乐编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10245")
    @NotNull(message = "来源音乐不能为空")
    private Long id;

    @Schema(description = "续写起点（秒），不能超过原曲时长", requiredMode = Schema.RequiredMode.REQUIRED, example = "176")
    @NotNull(message = "续写起点不能为空")
    @Min(value = 0, message = "续写起点不能小于 0")
    private Integer extendSeconds;

    @Schema(description = "续写的歌词", example = "[Bridge]\n风停了 灯还亮着")
    @Size(max = 1024, message = "歌词不能超过 1024 个字符")
    private String lyric;

    @Schema(description = "音乐风格", example = "pop,female vocals,summer")
    @Size(max = 120, message = "音乐风格不能超过 120 个字符")
    private String tags;

    @Schema(description = "歌曲名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "夏夜晚风")
    @NotBlank(message = "请输入歌曲名称")
    @Size(max = 100, message = "歌曲名称不能超过 100 个字符")
    private String title;

}

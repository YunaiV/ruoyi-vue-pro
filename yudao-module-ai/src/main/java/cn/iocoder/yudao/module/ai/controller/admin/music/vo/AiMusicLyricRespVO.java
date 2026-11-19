package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 音乐生成歌词 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐生成歌词 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMusicLyricRespVO {

    @Schema(description = "歌曲名称，取生成内容的第一行", example = "海边的毕业季")
    private String title;

    @Schema(description = "歌词", requiredMode = Schema.RequiredMode.REQUIRED, example = "[Verse]\n背包里装满了夏天")
    private String lyric;

}

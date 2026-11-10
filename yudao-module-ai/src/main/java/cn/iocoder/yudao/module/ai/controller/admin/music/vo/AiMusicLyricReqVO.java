package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 音乐生成歌词 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐生成歌词 Request VO")
@Data
public class AiMusicLyricReqVO {

    @Schema(description = "歌词主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "毕业旅行，和朋友一起去海边")
    @NotBlank(message = "请输入歌词主题")
    @Size(max = 1024, message = "歌词主题不能超过 1024 个字符")
    private String prompt;

}

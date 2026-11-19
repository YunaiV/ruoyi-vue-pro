package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 音乐合并为完整歌曲 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐合并为完整歌曲 Request VO")
@Data
public class AiMusicConcatReqVO {

    @Schema(description = "续写作品的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10247")
    @NotNull(message = "来源音乐不能为空")
    private Long id;

}

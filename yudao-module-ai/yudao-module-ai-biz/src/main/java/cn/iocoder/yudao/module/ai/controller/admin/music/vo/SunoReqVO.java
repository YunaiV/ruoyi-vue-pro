package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

// TODO @xin：1）ai 前缀；2）AiSunoGenerateReqVO，要有生成哈。3）swaggger 缺少的属性，也最好加下，类似 example，类上的 swagger 等
/**
 * @author xiaoxin
 */
@Data
public class SunoReqVO {

    @Schema(description = "用于生成音乐音频的提示")
    private String prompt;

    @Schema(description = "是否纯音乐")
    private Boolean makeInstrumental;

    // TODO @xin：我们自己是不是用 modelVersion？还是什么梗精准，减少非必要的缩写
    @Schema(description = "模型版本")
    private String mv;

    @Schema(description = "音乐风格")
    private List<String> tags;

    @Schema(description = "音乐/歌曲名称")
    private String title;

    @Schema(description = "平台")
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "生成模式 lyric(歌词模式), description(描述模式)")
    @NotBlank(message = "生成模式不能为空")
    private String generateMode;

}
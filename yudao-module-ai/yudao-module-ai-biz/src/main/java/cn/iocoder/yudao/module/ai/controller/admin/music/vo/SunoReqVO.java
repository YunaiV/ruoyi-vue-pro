package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class SunoReqVO {
    /**
     * 用于生成音乐音频的提示
     */
    private String prompt;

    /**
     * 用于生成音乐音频的歌词
     */
    private String lyric;

    /**
     * 指示音乐音频是否为定制，如果为 true，则从歌词生成，否则从提示生成
     */
    private boolean custom;

    /**
     * 音乐音频的标题
     */
    private String title;

    /**
     * 音乐音频的风格
     */
    private String style;

    /**
     * 音乐音频生成后回调的 URL
     */
    private String callbackUrl;
}
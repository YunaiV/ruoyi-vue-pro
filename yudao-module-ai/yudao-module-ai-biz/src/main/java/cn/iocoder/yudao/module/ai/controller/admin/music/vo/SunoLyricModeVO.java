package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import lombok.Data;

/**
 * @Author jxli@quant360.com
 * @Date 2024/6/7
 */
@Data
public class SunoLyricModeVO extends SunoReqVO {

    /**
     * 标签/音乐风格
     */
    private String tags;

    /**
     * 音乐名称
     */
    private String title;

}

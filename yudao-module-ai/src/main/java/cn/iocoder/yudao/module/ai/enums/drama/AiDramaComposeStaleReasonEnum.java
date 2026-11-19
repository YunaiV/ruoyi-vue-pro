package cn.iocoder.yudao.module.ai.enums.drama;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 短剧成片需要重新合成的原因的枚举
 *
 * 按判断的先后顺序排列；原因文案直接返回给前端展示，不落库
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaComposeStaleReasonEnum {

    STORYBOARD_DELETED("成片使用的镜头已删除"),
    VIDEO_REPLACED("成片使用的镜头视频已更换"),
    STORYBOARD_ADDED("成片合成后新增了镜头"),
    SCRIPT_UPDATED("剧本的分镜已更新");

    /**
     * 原因
     */
    private final String reason;

}

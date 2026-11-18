package cn.iocoder.yudao.module.ai.enums.video;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 视频输入文件角色的枚举：作为 ai_video.input_urls 的键
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiVideoInputRoleEnum implements ArrayValuable<String> {

    FIRST_FRAME("first_frame", "首帧"),
    LAST_FRAME("last_frame", "尾帧"),
    REFERENCE_IMAGE("reference_image", "参考图"),
    REFERENCE_VIDEO("reference_video", "参考视频"),
    REFERENCE_AUDIO("reference_audio", "参考音频"),
    IMAGE("image", "人物图片"), // 数字人口播、动作模仿
    AUDIO("audio", "驱动音频"), // 数字人口播
    VIDEO("video", "动作视频"); // 动作模仿

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiVideoInputRoleEnum::getRole).toArray(String[]::new);

    /**
     * 角色
     */
    private final String role;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}

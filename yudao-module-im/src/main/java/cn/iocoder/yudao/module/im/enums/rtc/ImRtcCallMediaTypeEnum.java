package cn.iocoder.yudao.module.im.enums.rtc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * IM 通话媒体类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImRtcCallMediaTypeEnum implements ArrayValuable<Integer> {

    VOICE(1, "语音"), // 仅音频
    VIDEO(2, "视频"); // 音频 + 视频

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImRtcCallMediaTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isVoice(Integer type) {
        return Objects.equals(VOICE.type, type);
    }

    public static boolean isVideo(Integer type) {
        return Objects.equals(VIDEO.type, type);
    }

}

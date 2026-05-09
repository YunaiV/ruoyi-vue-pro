package cn.iocoder.yudao.module.im.enums.rtc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 通话媒体类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImCallMediaTypeEnum implements ArrayValuable<Integer> {

    VOICE(1, "语音"),
    VIDEO(2, "视频");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImCallMediaTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

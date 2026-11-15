package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧画幅的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaAspectRatioEnum implements ArrayValuable<String> {

    RATIO_9_16("9:16", "竖屏"),
    RATIO_16_9("16:9", "横屏"),
    RATIO_4_3("4:3", "横版"),
    RATIO_3_4("3:4", "竖版"),
    RATIO_1_1("1:1", "方形"),
    RATIO_21_9("21:9", "宽银幕");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiDramaAspectRatioEnum::getRatio).toArray(String[]::new);

    /**
     * 画幅
     */
    private final String ratio;
    /**
     * 画幅名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}

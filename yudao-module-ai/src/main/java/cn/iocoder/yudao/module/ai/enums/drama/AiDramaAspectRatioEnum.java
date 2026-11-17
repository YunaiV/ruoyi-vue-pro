package cn.iocoder.yudao.module.ai.enums.drama;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * AI 短剧画幅的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaAspectRatioEnum implements ArrayValuable<String> {

    RATIO_9_16("9:16", "竖屏", "1440x2560"),
    RATIO_16_9("16:9", "横屏", "2560x1440"),
    RATIO_4_3("4:3", "横版", "2304x1728"),
    RATIO_3_4("3:4", "竖版", "1728x2304"),
    RATIO_1_1("1:1", "方形", "2048x2048"),
    RATIO_21_9("21:9", "宽银幕", "3024x1296");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiDramaAspectRatioEnum::getRatio).toArray(String[]::new);

    /**
     * 画幅
     */
    private final String ratio;
    /**
     * 画幅名
     */
    private final String name;
    /**
     * Seedream 的出图尺寸：用于镜头起始帧，像素数与 2048x2048 相近
     */
    private final String seedreamSize;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static AiDramaAspectRatioEnum valueOfRatio(String ratio) {
        return ArrayUtil.firstMatch(o -> Objects.equals(o.getRatio(), ratio), values());
    }

}

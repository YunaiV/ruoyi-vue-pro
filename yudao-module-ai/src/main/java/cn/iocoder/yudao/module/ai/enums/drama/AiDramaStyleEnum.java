package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧画风的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaStyleEnum implements ArrayValuable<Integer> {

    SCRIPT_TONE(1, "随剧本基调"),
    REALISTIC(2, "真人写实"),
    JAPANESE_ANIME(3, "日系动漫"),
    CHINESE_COMIC(4, "国漫"),
    CHINESE_3D(5, "国风 3D"),
    AMERICAN_COMIC(6, "美式漫画");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaStyleEnum::getStyle).toArray(Integer[]::new);

    /**
     * 画风
     */
    private final Integer style;
    /**
     * 画风名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

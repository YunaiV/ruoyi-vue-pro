package cn.iocoder.yudao.module.ai.enums.image;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 绘图生成方式的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiImageModeEnum implements ArrayValuable<Integer> {

    DRAW(10, "AI 生图"), // 通用模型，含 Seedream
    MIDJOURNEY_IMAGINE(20, "MJ 文生图"),
    MIDJOURNEY_IMAGE(21, "MJ 图生图"),
    MIDJOURNEY_BLEND(22, "MJ 融图"),
    MIDJOURNEY_SWAP_FACE(23, "MJ 换脸"),
    MIDJOURNEY_CONSISTENCY(24, "MJ 一致性"),
    MIDJOURNEY_ACTION(30, "MJ 按钮动作"),
    MIDJOURNEY_MODAL(31, "MJ 局部重绘");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiImageModeEnum::getMode).toArray(Integer[]::new);

    /**
     * 生成方式
     */
    private final Integer mode;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

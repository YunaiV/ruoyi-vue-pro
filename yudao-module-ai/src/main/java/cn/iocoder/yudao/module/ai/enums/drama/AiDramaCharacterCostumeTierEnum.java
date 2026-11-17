package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧角色服装华丽度的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaCharacterCostumeTierEnum implements ArrayValuable<Integer> {

    PLAIN(1, "朴素／功能级"),
    DAILY(2, "日常／普通级"),
    QUALITY(3, "专业／品质级"),
    ELITE(4, "贵族／精英级"),
    ROYAL(5, "皇室／顶奢级");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaCharacterCostumeTierEnum::getTier).toArray(Integer[]::new);

    /**
     * 华丽度
     */
    private final Integer tier;
    /**
     * 华丽度名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

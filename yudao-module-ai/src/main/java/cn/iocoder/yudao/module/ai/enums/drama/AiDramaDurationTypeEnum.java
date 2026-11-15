package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧公开广场时长筛选的枚举：按成片时长分档
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaDurationTypeEnum implements ArrayValuable<Integer> {

    SHORT(1, "30 秒内", null, 30),
    MEDIUM(2, "30-60 秒", 30, 60),
    LONG(3, "1 分钟以上", 60, null);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaDurationTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;
    /**
     * 最小时长（秒，包含）；为空表示不限制
     */
    private final Integer minDuration;
    /**
     * 最大时长（秒，不包含）；为空表示不限制
     */
    private final Integer maxDuration;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static AiDramaDurationTypeEnum of(Integer type) {
        return Arrays.stream(values()).filter(item -> item.getType().equals(type)).findFirst().orElse(null);
    }

}

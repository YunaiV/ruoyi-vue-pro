package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧剧本创作模式的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaScriptCreationModeEnum implements ArrayValuable<Integer> {

    STANDARD(1, "标准");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaScriptCreationModeEnum::getMode).toArray(Integer[]::new);

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 模式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

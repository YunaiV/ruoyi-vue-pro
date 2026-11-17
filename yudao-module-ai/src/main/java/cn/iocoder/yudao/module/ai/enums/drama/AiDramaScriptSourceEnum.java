package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧剧本来源的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaScriptSourceEnum implements ArrayValuable<Integer> {

    MANUAL(1, "手动编写"),
    LLM(2, "模型生成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaScriptSourceEnum::getSource).toArray(Integer[]::new);

    /**
     * 来源
     */
    private final Integer source;
    /**
     * 来源名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

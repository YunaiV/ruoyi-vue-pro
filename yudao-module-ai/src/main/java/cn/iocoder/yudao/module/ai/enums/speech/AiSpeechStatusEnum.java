package cn.iocoder.yudao.module.ai.enums.speech;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 语音状态的枚举
 *
 * 状态链：10 生成中 → 20 已完成；任一步失败或超时进入 30 已失败。已完成、已失败为终态，不再变化
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiSpeechStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(10, "生成中"),
    SUCCESS(20, "已完成"),
    FAIL(30, "已失败");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiSpeechStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 写作类型的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiWriteTypeEnum implements IntArrayValuable {

    DESCRIPTION(1, "撰写"),
    LYRIC(2, "回复");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiWriteTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

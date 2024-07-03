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
public enum AiWriteLengthEnum implements IntArrayValuable {

    AUTO(1, "自动"),
    SHORT(2, "短"),
    MEDIUM(3, "中"),
    LONG(4, "长");

    /**
     * 长度
     */
    private final Integer length;
    /**
     * 长度名
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiWriteLengthEnum::getLength).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static AiWriteLengthEnum valueOfLength(Integer length) {
        for (AiWriteLengthEnum lengthEnum : AiWriteLengthEnum.values()) {
            if (lengthEnum.getLength().equals(length)) {
                return lengthEnum;
            }
        }
        throw new IllegalArgumentException("未知长度： " + length);
    }
}

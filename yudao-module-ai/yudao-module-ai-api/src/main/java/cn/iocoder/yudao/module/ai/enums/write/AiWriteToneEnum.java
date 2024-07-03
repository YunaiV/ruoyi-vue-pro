package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AiWriteToneEnum implements IntArrayValuable {

    AUTO(1, "自动"),
    FRIENDLY(2, "友善"),
    CASUAL(3, "随意"),
    KIND(4, "友好"),
    PROFESSIONAL(5, "专业"),
    HUMOROUS(6, "谈谐"),
    INTERESTING(7, "有趣"),
    FORMAL(8, "正式");

    /**
     * 语气
     */
    private final Integer tone;
    /**
     * 语气名
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiWriteToneEnum::getTone).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static AiWriteToneEnum valueOfTone(Integer tone) {
        for (AiWriteToneEnum toneEnum : AiWriteToneEnum.values()) {
            if (toneEnum.getTone().equals(tone)) {
                return toneEnum;
            }
        }
        throw new IllegalArgumentException("未知语气： " + tone);
    }
}
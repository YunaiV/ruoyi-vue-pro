package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 任务监听器键值对类型
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmListenerParamTypeEnum implements IntArrayValuable {

    FIXED_VALUE(1, "固定值"),
    FROM_FORM(2, "表单");

    private final Integer type;
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmListenerParamTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
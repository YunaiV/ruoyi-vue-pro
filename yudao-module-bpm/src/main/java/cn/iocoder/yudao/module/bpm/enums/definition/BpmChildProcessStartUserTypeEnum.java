package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 子流程发起人类型枚举
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmChildProcessStartUserTypeEnum implements ArrayValuable<Integer> {

    MAIN_PROCESS_START_USER(1, "同主流程发起人"),
    FROM_FORM(2, "表单");

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmChildProcessStartUserTypeEnum::getType).toArray(Integer[]::new);

    public static BpmChildProcessStartUserTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}

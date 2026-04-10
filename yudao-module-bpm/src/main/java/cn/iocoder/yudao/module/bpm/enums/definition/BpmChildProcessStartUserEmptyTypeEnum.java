package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 当子流程发起人为空时类型枚举
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmChildProcessStartUserEmptyTypeEnum implements ArrayValuable<Integer> {

    MAIN_PROCESS_START_USER(1, "同主流程发起人"),
    CHILD_PROCESS_ADMIN(2, "子流程管理员"),
    MAIN_PROCESS_ADMIN(3, "主流程管理员");

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmChildProcessStartUserEmptyTypeEnum::getType).toArray(Integer[]::new);

    public static BpmChildProcessStartUserEmptyTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}

package cn.iocoder.yudao.module.oa.enums.task;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 任务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaTaskTypeEnum implements ArrayValuable<Integer> {

    WORK(1, "公事"),
    PERSONAL(2, "私事");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaTaskTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

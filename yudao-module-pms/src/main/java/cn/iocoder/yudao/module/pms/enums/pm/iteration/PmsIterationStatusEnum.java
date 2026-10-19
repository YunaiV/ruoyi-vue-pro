package cn.iocoder.yudao.module.pms.enums.pm.iteration;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 迭代状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsIterationStatusEnum implements ArrayValuable<Integer> {

    PLANNED(1, "未开始"),
    ACTIVE(2, "进行中"),
    COMPLETED(3, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsIterationStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsIterationStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}

package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 工作项语义状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemStatusTypeEnum implements ArrayValuable<Integer> {

    PENDING(1, "未开始"),
    PROCESSING(2, "进行中"),
    COMPLETED(3, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsWorkItemStatusTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 状态类型
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

    public static PmsWorkItemStatusTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

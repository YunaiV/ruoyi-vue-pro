package cn.iocoder.yudao.module.hrm.enums.employee.employment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * HRM 员工异动类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeChangeTypeEnum implements ArrayValuable<Integer> {

    REGULAR(4, "转正"),
    TRANSFER(5, "调岗"),
    PROMOTION(6, "晋升"),
    DEMOTION(7, "降级"),
    FULL_TIME(8, "转为全职"),
    REHIRE(9, "再入职");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeChangeTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 需要预约生效的异动类型集合
     */
    public static final Set<Integer> PENDING_EFFECT_TYPES = SetUtils.asSet(
            REGULAR.getType(), TRANSFER.getType(), PROMOTION.getType(), DEMOTION.getType(), FULL_TIME.getType());

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeChangeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

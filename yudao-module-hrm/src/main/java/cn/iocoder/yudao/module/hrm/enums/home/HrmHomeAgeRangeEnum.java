package cn.iocoder.yudao.module.hrm.enums.home;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 团队工作台年龄区间枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmHomeAgeRangeEnum implements ArrayValuable<Integer> {

    UNDER_18(1, "17 以下", 17),
    AGE_18_TO_25(2, "18 至 25", 25),
    AGE_26_TO_35(3, "26 至 35", 35),
    AGE_36_TO_45(4, "36 至 45", 45),
    AGE_46_TO_55(5, "46 至 55", 55),
    AGE_56_AND_ABOVE(6, "56 以上", Integer.MAX_VALUE);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmHomeAgeRangeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;
    private final Integer maximumAge;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据年龄获得区间类型
     *
     * @param age 年龄
     * @return 区间类型；年龄未填写或非法时返回 {@code null}
     */
    public static Integer getType(Integer age) {
        if (age == null || age < 0) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> age <= item.maximumAge)
                .findFirst().map(HrmHomeAgeRangeEnum::getType).orElse(null);
    }

}

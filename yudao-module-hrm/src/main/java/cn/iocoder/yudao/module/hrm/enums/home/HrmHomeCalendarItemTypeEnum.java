package cn.iocoder.yudao.module.hrm.enums.home;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 首页日历事项类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmHomeCalendarItemTypeEnum implements ArrayValuable<Integer> {

    NOTE(1, "备忘"),
    BIRTHDAY(2, "生日"),
    ENTRY(3, "入职"),
    REGULAR(4, "转正"),
    LEAVE(5, "离职"),
    RECRUIT(6, "招聘"),
    ATTENDANCE(7, "考勤");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmHomeCalendarItemTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmHomeCalendarItemTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

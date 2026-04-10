package cn.iocoder.yudao.module.mes.enums.cal;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 假期类型枚举
 *
 * 对应字典 mes_cal_holiday_type
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesCalHolidayTypeEnum implements ArrayValuable<Integer> {

    WORKDAY(1, "工作日"),
    HOLIDAY(2, "节假日");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(MesCalHolidayTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

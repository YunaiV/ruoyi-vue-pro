package cn.iocoder.yudao.module.mes.enums.cal;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * MES 轮班方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesCalShiftTypeEnum implements ArrayValuable<Integer> {

    SINGLE(1, "单白班", ListUtil.of(
            Pair.of("白班", new String[]{"08:00", "18:00"})
    )),
    TWO(2, "两班倒", ListUtil.of(
            Pair.of("白班", new String[]{"08:00", "20:00"}),
            Pair.of("夜班", new String[]{"20:00", "08:00"})
    )),
    THREE(3, "三班倒", ListUtil.of(
            Pair.of("白班", new String[]{"08:00", "16:00"}),
            Pair.of("中班", new String[]{"16:00", "00:00"}),
            Pair.of("夜班", new String[]{"00:00", "08:00"})
    ));

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesCalShiftTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;
    /**
     * 班次配置列表
     *
     * key: 班次名称（如 "白班", "夜班", "中班"）
     * value: [startTime, endTime]（如 ["08:00", "18:00"]）
     */
    private final List<Pair<String, String[]>> shifts;

    /**
     * 获取班次数量上限
     */
    public int getShiftCount() {
        return shifts.size();
    }

    /**
     * 获取所需的最少班组数量
     */
    public int getRequiredTeamCount() {
        return shifts.size();
    }

    /**
     * 根据类型值获取枚举
     */
    public static MesCalShiftTypeEnum valueOf(Integer type) {
        for (MesCalShiftTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        throw new IllegalArgumentException("不支持的轮班方式: " + type);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

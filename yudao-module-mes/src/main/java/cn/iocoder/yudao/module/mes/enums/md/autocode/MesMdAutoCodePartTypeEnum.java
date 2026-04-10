package cn.iocoder.yudao.module.mes.enums.md.autocode;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 编码规则分段类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesMdAutoCodePartTypeEnum implements ArrayValuable<Integer> {

    INPUT_CHAR(1, "输入字符"),
    DATE(2, "当前日期"),
    FIXED_CHAR(3, "固定字符"),
    SERIAL_NUMBER(4, "流水号");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesMdAutoCodePartTypeEnum::getType).toArray(Integer[]::new);

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

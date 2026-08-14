package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工证件类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeIdTypeEnum implements ArrayValuable<Integer> {

    ID_CARD(1, "身份证"),
    HONG_KONG_MACAO_PASS(2, "港澳通行证"),
    TAIWAN_PASS(3, "台湾通行证"),
    PASSPORT(4, "护照"),
    OTHER(5, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeIdTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmEmployeeIdTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

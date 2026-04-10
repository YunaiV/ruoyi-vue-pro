package cn.iocoder.yudao.module.mes.enums.md.autocode;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 编码规则补齐方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesMdAutoCodePaddedMethodEnum implements ArrayValuable<Integer> {

    LEFT(1, "左补齐"),
    RIGHT(2, "右补齐");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesMdAutoCodePaddedMethodEnum::getMethod).toArray(Integer[]::new);

    /**
     * 方式
     */
    private final Integer method;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

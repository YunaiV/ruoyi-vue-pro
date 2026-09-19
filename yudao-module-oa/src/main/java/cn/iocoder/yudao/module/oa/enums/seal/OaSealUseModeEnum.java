package cn.iocoder.yudao.module.oa.enums.seal;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用印方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSealUseModeEnum implements ArrayValuable<Integer> {

    ONSITE(1, "现场用印"),
    BORROW(2, "外借");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSealUseModeEnum::getMode).toArray(Integer[]::new);

    /**
     * 用印方式
     */
    private final Integer mode;
    /**
     * 名称
     */
    private final String name;

    public static OaSealUseModeEnum valueOf(Integer mode) {
        return ArrayUtil.firstMatch(item -> item.getMode().equals(mode), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

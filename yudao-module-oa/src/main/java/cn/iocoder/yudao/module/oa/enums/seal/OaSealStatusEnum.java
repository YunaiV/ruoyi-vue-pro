package cn.iocoder.yudao.module.oa.enums.seal;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 印章台账状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSealStatusEnum implements ArrayValuable<Integer> {

    AVAILABLE(0, "在库"),
    DISABLED(1, "停用"),
    IN_USE(2, "使用中");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSealStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 印章台账状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    public static OaSealStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

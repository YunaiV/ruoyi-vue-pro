package cn.iocoder.yudao.module.oa.enums.seal;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用印类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSealApplyTypeEnum implements ArrayValuable<Integer> {

    CONTRACT(1, "合同用印"),
    AGREEMENT(2, "协议用印"),
    CERTIFICATE(3, "证明用印"),
    AUTHORIZATION(4, "授权用印"),
    OTHER(5, "其他用印");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSealApplyTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 用印类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static OaSealApplyTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

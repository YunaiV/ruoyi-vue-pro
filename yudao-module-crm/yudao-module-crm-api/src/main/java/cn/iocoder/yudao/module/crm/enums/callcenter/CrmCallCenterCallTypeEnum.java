package cn.iocoder.yudao.module.crm.enums.callcenter;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 呼叫中心厂商枚举
 *
 * @author lzxhqs
 */
@RequiredArgsConstructor
@Getter
public enum CrmCallCenterCallTypeEnum implements IntArrayValuable {

    CLUE(1, "线索池"),
    CUSTOMER(2, "客户池");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCallCenterCallTypeEnum::getType).toArray();

    /**
     * 场景类型
     */
    private final Integer type;
    /**
     * 场景名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static CrmCallCenterCallTypeEnum fromStatus(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}

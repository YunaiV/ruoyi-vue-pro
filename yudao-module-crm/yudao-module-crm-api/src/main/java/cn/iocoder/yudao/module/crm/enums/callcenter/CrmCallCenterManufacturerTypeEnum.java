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
public enum CrmCallCenterManufacturerTypeEnum implements IntArrayValuable {

    YUNKE(1, "云客"),
    LIANLIAN(2, "连连");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCallCenterManufacturerTypeEnum::getType).toArray();

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

    public static CrmCallCenterManufacturerTypeEnum fromStatus(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}

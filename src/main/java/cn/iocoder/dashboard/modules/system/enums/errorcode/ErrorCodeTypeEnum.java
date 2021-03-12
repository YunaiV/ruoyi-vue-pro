package cn.iocoder.dashboard.modules.system.enums.errorcode;

import cn.iocoder.dashboard.common.core.IntArrayValuable;

import java.util.Arrays;

/**
 * @author dylan
 */

public enum ErrorCodeTypeEnum implements IntArrayValuable {

    /**
     * 自动生成
     */
    AUTO_GENERATION(1),
    /**
     * 手动编辑
     */
    MANUAL_OPERATION(2);

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErrorCodeTypeEnum::getType).toArray();

    private final Integer type;

    ErrorCodeTypeEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}

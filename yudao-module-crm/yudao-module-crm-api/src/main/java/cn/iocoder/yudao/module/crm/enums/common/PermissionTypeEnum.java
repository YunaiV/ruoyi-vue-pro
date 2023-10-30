package cn.iocoder.yudao.module.crm.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PermissionTypeEnum implements IntArrayValuable {

    READONLY(1, "只读"),
    READ_AND_WRITE(2, "读写");
    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PermissionTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

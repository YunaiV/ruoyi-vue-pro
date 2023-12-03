package cn.iocoder.yudao.module.crm.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author ZanGe丶
 * @create 2023-11-30 21:53
 */
@Getter
@AllArgsConstructor
public enum CrmProductStatusEnum implements IntArrayValuable {
    DISABLE(0, "下架"),
    ENABLE(1, "上架");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmProductStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    /**
     * 判断是否处于【上架】状态
     *
     * @param status 状态
     * @return 是否处于【上架】状态
     */
    public static boolean isEnable(Integer status) {
        return ENABLE.getStatus().equals(status);
    }
}

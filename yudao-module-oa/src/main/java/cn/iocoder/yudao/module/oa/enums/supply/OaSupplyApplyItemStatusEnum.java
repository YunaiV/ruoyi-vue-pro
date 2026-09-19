package cn.iocoder.yudao.module.oa.enums.supply;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 办公用品申请明细状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSupplyApplyItemStatusEnum implements ArrayValuable<Integer> {

    APPLYING(-1, "申请中"),
    PENDING(0, "待发放"),
    RECEIVED(1, "已领用"),
    RETURN_PENDING(2, "待归还"),
    RETURNED(3, "已归还");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSupplyApplyItemStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

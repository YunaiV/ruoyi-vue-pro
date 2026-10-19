package cn.iocoder.yudao.module.oa.enums.supply;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用品管理类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSupplyManageTypeEnum implements ArrayValuable<Integer> {

    CONSUMABLE(1, "消耗品"),
    BORROWABLE(2, "借用品"),
    ASSET_CANDIDATE(3, "资产品");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSupplyManageTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 用品管理类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static OaSupplyManageTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

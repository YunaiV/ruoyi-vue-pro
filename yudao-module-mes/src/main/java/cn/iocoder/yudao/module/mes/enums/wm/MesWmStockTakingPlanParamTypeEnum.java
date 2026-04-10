package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 盘点方案参数类型枚举
 */
@Getter
@AllArgsConstructor
public enum MesWmStockTakingPlanParamTypeEnum implements ArrayValuable<Integer> {

    WAREHOUSE(MesBizTypeConstants.WM_WAREHOUSE, "仓库"),
    LOCATION(MesBizTypeConstants.WM_LOCATION, "库区"),
    AREA(MesBizTypeConstants.WM_AREA, "库位"),
    ITEM(MesBizTypeConstants.MD_ITEM, "物料"),
    BATCH(MesBizTypeConstants.WM_BATCH, "批次"),
    QUALITY_STATUS(MesBizTypeConstants.QUALITY_STATUS, "质量状态");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(MesWmStockTakingPlanParamTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 参数类型
     */
    private final Integer type;
    /**
     * 类型名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

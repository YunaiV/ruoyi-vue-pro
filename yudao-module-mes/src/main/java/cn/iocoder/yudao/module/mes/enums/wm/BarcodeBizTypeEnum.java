package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 条码业务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BarcodeBizTypeEnum implements ArrayValuable<Integer> {

    WAREHOUSE(MesBizTypeConstants.WM_WAREHOUSE, "仓库"),
    LOCATION(MesBizTypeConstants.WM_LOCATION, "库区"),
    AREA(MesBizTypeConstants.WM_AREA, "库位"),
    PACKAGE(MesBizTypeConstants.WM_PACKAGE, "装箱单"),
    STOCK(MesBizTypeConstants.WM_STOCK, "库存"),
    BATCH(MesBizTypeConstants.WM_BATCH, "批次"),
    PROCARD(MesBizTypeConstants.PRO_CARD, "流转卡"),
    WORKORDER(MesBizTypeConstants.PRO_WORKORDER, "工单"),
    TRANSORDER(MesBizTypeConstants.PRO_TRANS_ORDER, "流转单"),
    MACHINERY(MesBizTypeConstants.DV_MACHINERY, "设备"),
    TOOL(MesBizTypeConstants.TM_TOOL, "工具"),
    ITEM(MesBizTypeConstants.MD_ITEM, "物料"),
    VENDOR(MesBizTypeConstants.MD_VENDOR, "供应商"),
    CLIENT(MesBizTypeConstants.MD_CLIENT, "客户"),
    WORKSTATION(MesBizTypeConstants.MD_WORKSTATION, "工作站"),
    WORKSHOP(MesBizTypeConstants.MD_WORKSHOP, "车间"),
    USER(MesBizTypeConstants.MD_USER, "人员");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BarcodeBizTypeEnum::getValue).toArray(Integer[]::new);

    /**
     * 业务类型值
     */
    private final Integer value;
    /**
     * 业务类型名称
     */
    private final String label;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

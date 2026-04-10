package cn.iocoder.yudao.module.mes.enums.qc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 质检来源单据类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesQcSourceDocTypeEnum implements ArrayValuable<Integer> {

    // === IQC 来源单据 ===
    ARRIVAL_NOTICE(MesBizTypeConstants.WM_ARRIVAL_NOTICE, "到货通知单"),
    OUTSOURCE_RECPT(MesBizTypeConstants.WM_OUTSOURCE_RECPT, "外协入库单"),

    // === IPQC 来源单据 ===
    PRO_FEEDBACK(MesBizTypeConstants.PRO_FEEDBACK, "生产报工"),

    // === OQC 来源单据 ===
    PRODUCT_SALES(MesBizTypeConstants.WM_PRODUCT_SALES, "销售出库单"),

    // === RQC 来源单据 ===
    RETURN_ISSUE(MesBizTypeConstants.WM_RETURN_ISSUE, "生产退料单"),
    RETURN_SALES(MesBizTypeConstants.WM_RETURN_SALES, "销售退货单"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesQcSourceDocTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

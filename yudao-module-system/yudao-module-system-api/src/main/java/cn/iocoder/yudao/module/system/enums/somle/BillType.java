package cn.iocoder.yudao.module.system.enums.somle;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 单据类型
 **/
@RequiredArgsConstructor
@Getter
public enum BillType implements ArrayValuable<Integer>, DictEnum {

    // ========== WMS  编码段 0~99 ==========
    WMS_INBOUND(0, "入库单"),
    WMS_OUTBOUND(1, "出库单"),
    WMS_STOCKCHECK(2, "盘点单"),
    WMS_EXCHANGE(3,"换货单"),
    WMS_PICKUP(4,"上架单"),
    WMS_BIN_MOVE(5,"库位移动单"),

    // ========== TMS  编码段 100~200 ==========
    TMS_FIRST_MILE(100, "头程单"),
    TMS_TRANSFER(101, "调拨单"),
    TMS_LAST_MILE(102, "尾程单"),
    TMS_FIRST_MILE_REQUEST(103, "头程单申请"),

    // ========== SRM  编码段 201~300 ==========
    SRM_PURCHASE_IN(201, "到货单"),
    SRM_PURCHASE_RETURN(202, "退货单"),
    ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(BillType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static BillType parse(Integer value) {
        for (BillType e : BillType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static BillType parse(String nameOrLabel) {
        for (BillType e : BillType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (BillType e : BillType.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Integer[] array() {
        return VALUES;
    }
}

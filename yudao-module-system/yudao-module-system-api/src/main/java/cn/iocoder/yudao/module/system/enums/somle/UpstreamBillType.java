package cn.iocoder.yudao.module.system.enums.somle;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 上游单据类型，外部类型数据追踪BillType枚举
 **/
@RequiredArgsConstructor
@Getter
public enum UpstreamBillType implements ArrayValuable<Integer>, DictEnum {

    //手工生成
    WMS_MANUAL(0, "手工入库"),
    WMS_PURCHASE(1, "采购入库"),
    WMS_STOCKCHECK(2, "盘点入库"),
    //外部生成
    TMS_FIRST_MILE(100, "头程单入库"),
    TMS_TRANSFER(101, "调拨单入库"),
    SRM_PURCHASE_IN(201, "到货单入库"),
    SRM_PURCHASE_RETURN(202, "退货单入库"),
    ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(UpstreamBillType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static UpstreamBillType parse(Integer value) {
        for (UpstreamBillType e : UpstreamBillType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static UpstreamBillType parse(String nameOrLabel) {
        for (UpstreamBillType e : UpstreamBillType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (UpstreamBillType e : UpstreamBillType.values()) {
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

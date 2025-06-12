package cn.iocoder.yudao.module.wms.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 流水发生的原因
 *
 * @author jisencai*/
@RequiredArgsConstructor
@Getter
public enum WmsStockReason implements ArrayValuable<Integer>, DictEnum {

    INBOUND(1, "入库"),
    PICKUP(2, "拣货"),
    OUTBOUND_FINISH(3, "完成出库"),
    OUTBOUND_AGREE(10, "同意出库单"),
    OUTBOUND_SUBMIT(4, "提交出库单"),
    OUTBOUND_REJECT(5, "拒绝出库单"),
    STOCK_BIN_MOVE(6,"库位库存移动"),
    STOCK_LOGIC_MOVE(7, "逻辑库存移动"),
    STOCKCHECK_POSITIVE(8, "盘赢"),
    STOCKCHECK_NEGATIVE(9, "盘亏"),
    EXCHANGE(10, "良次转换"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsStockReason::getValue).toArray(Integer[]::new);

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsStockReason parse(Integer value) {
        for (WmsStockReason e : WmsStockReason.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsStockReason parse(String nameOrLabel) {
        for (WmsStockReason e : WmsStockReason.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsStockReason e : WmsStockReason.values()) {
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

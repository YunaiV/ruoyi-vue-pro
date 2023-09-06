package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金记录状态枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageRecordStatusEnum implements IntArrayValuable {

    WAIT_SETTLEMENT(0, "待结算"),
    SETTLEMENT(1, "已结算"),
    CANCEL(2, "已取消"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BrokerageRecordStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

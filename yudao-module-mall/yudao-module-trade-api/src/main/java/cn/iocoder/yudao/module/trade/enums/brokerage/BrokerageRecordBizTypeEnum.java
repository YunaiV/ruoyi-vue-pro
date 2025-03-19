package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金记录业务类型枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageRecordBizTypeEnum implements ArrayValuable<Integer> {

    ORDER(1, "获得推广佣金", "获得推广佣金 {}", true),
    WITHDRAW(2, "提现申请", "提现申请扣除佣金 {}", false),
    WITHDRAW_REJECT(3, "提现申请驳回", "提现申请驳回，返还佣金 {}", true),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageRecordBizTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 标题
     */
    private final String title;
    /**
     * 描述
     */
    private final String description;
    /**
     * 是否为增加佣金
     */
    private final boolean add;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

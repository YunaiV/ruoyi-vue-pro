package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 分佣模式枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageEnabledConditionEnum implements IntArrayValuable {

    /**
     * 所有用户都可以分销
     */
    ALL(1, "人人分销"),
    /**
     * 仅可后台手动设置推广员
     */
    ADMIN(2, "指定分销"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BrokerageEnabledConditionEnum::getCondition).toArray();

    /**
     * 模式
     */
    private final Integer condition;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

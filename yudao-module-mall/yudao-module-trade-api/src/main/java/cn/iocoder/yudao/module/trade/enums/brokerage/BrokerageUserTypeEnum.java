package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 分销用户类型枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageUserTypeEnum implements IntArrayValuable {

    ALL(0, "全部"),
    FIRST(1, "一级推广人"),
    SECOND(2, "二级推广人"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BrokerageUserTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

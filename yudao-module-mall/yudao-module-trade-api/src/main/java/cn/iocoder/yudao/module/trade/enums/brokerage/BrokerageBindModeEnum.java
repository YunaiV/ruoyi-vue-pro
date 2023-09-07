package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 分销关系绑定模式枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageBindModeEnum implements IntArrayValuable {

    // TODO @疯狂：要不从 1 开始？
    /**
     * 只要用户没有推广人，随时都可以绑定分销关系
     */
    ANYTIME(0, "没有推广人"),
    /**
     * 仅新用户注册时才能绑定推广关系
     */
    REGISTER(1, "新用户"),
    // TODO @疯狂：要加个 2，每次扫码都覆盖
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BrokerageBindModeEnum::getMode).toArray();

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

package cn.iocoder.yudao.module.jl.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Objects;

/**
 * 支付订单的状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SalesLeadSourceEnums implements IntArrayValuable {

    ALL(0, "所有"),
    Offline(1, "线下拓展"),
    Online(2, "线上拓展"),
    DealAgain(3, "老客户再成绩"),
    ;

    private final Integer status;
    private final String name;

    @Override
    public int[] array() {
        return new int[0];
    }

    /**
     * 判断是否支付成功
     *
     * @param status 状态
     * @return 是否支付成功
     */
    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, ALL.getStatus());
    }

}
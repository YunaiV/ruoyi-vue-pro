package cn.iocoder.yudao.module.pay.enums.refund;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单的退款状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayRefundTypeEnum implements IntArrayValuable {

    NO(0, "未退款"),
    SOME(10, "部分退款"),
    ALL(20, "全部退款")
    ;

    private final Integer status;
    private final String name;

    @Override
    public int[] array() {
        return new int[0];
    }

}

package cn.iocoder.yudao.module.pay.enums.order;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单的状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatusEnum implements IntArrayValuable {

    WAITING(0, "未支付"),
    SUCCESS(10, "支付成功"),
    CLOSED(20, "支付关闭"), // 未付款交易超时关闭，或支付完成后全额退款 TODO 芋艿：需要优化下
    ;

    private final Integer status;
    private final String name;

    @Override
    public int[] array() {
        return new int[0];
    }

}

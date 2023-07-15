package cn.iocoder.yudao.framework.pay.core.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 渠道的支付状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatusRespEnum {

    WAITING(0, "未支付"),
    SUCCESS(10, "支付成功"),
    CLOSED(20, "支付关闭"), // 未付款交易超时关闭，或支付完成后全额退款
    ;

    private final Integer status;
    private final String name;

    /**
     * 判断是否支付成功
     *
     * @param status 状态
     * @return 是否支付成功
     */
    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

}

package cn.iocoder.yudao.module.pay.enums.refund;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayRefundStatusEnum {

    CREATE(0, "退款订单生成"),
    SUCCESS(1, "退款成功"),
    FAILURE(2, "退款失败"),
    CLOSE(99, "退款关闭");

    private final Integer status;
    private final String name;
}

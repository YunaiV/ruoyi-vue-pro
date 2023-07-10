package cn.iocoder.yudao.framework.pay.core.enums.refund;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道的退款状态枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum PayRefundRespEnum {

    SUCCESS(1, "退款成功"),
    FAILURE(2, "退款失败"),
    PROCESSING(3,"退款处理中"),
    CLOSED(4, "退款关闭");

    private final Integer status;
    private final String name;

}

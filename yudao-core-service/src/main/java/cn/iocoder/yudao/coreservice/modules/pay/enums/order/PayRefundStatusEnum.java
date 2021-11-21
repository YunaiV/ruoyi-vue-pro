package cn.iocoder.yudao.coreservice.modules.pay.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayRefundStatusEnum {
    CREATE(0, "退款订单生成"),
    SUCCESS(1, "退款成功"),
    FAILURE(2, "退款失败"),
    PROCESSING_NOTIFY(3,"退款中, 渠道通知结果"),
    PROCESSING_QUERY(4,"退款中, 系统查询结果"),
    UNKNOWN_RETRY(5,"状态未知，需要重试"),
    UNKNOWN_QUERY(6,"状态未知，系统查询结果"),
    CLOSE(99, "退款关闭");

    private final Integer status;
    private final String name;
}

package cn.iocoder.yudao.module.pay.enums.notify;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付通知状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayNotifyStatusEnum {

    WAITING(0, "等待通知"),
    SUCCESS(10, "通知成功"),
    FAILURE(20, "通知失败"), // 多次尝试，彻底失败
    REQUEST_SUCCESS(21, "请求成功，但是结果失败"),
    REQUEST_FAILURE(22, "请求失败"),

    ;

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

}

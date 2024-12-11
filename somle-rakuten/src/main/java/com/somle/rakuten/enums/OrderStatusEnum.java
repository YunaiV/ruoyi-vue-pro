package com.somle.rakuten.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum OrderStatusEnum {

    // 定义订单状态及其对应的状态码和描述
    ORDER_CONFIRM_WAIT(100, "订单确认待"),
    PROCESSING(200, "处理中的订单"),
    WAITING_FOR_SHIPMENT(300, "等待发货"),
    CHANGE_CONFIRM_WAIT(400, "变更确认待"),
    SHIPPED(500, "已发货"),
    PAYMENT_IN_PROCESS(600, "支付处理中"),
    PAYMENT_COMPLETED(700, "支付完成"),
    CANCEL_CONFIRM_WAIT(800, "取消确认待"),
    CANCELLED(900, "已取消");

    // 获取状态码
    private final int code;          // 状态码
    // 获取状态描述
    private final String description; // 状态描述

    // 枚举构造函数，初始化状态码和描述
    OrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 根据状态码获取对应的枚举
    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code); // 如果状态码无效，抛出异常
    }

    // 判断订单是否完结
    public static boolean isCompleted(int statusCode) {
        // 完结的状态包括：已发货(500), 已支付(700), 已取消(900)
        return statusCode == SHIPPED.getCode() || statusCode == PAYMENT_COMPLETED.getCode() || statusCode == CANCELLED.getCode();
    }

    public static Boolean isSuccess(List<Integer> statusCodes) {
        //订单成功已发货+已支付
        return statusCodes.contains(SHIPPED.getCode()) && statusCodes.contains(PAYMENT_COMPLETED.getCode());
    }
}


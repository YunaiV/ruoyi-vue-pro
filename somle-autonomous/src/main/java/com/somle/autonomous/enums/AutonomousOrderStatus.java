package com.somle.autonomous.enums;

public enum AutonomousOrderStatus {

    /**
     * 订单已发货。
     */
    SHIPPED("Shipped", 1),

    /**
     * 订单已送达。
     */
    DELIVERED("Delivered", 2),

    /**
     * 订单已取消。
     */
    CANCEL("Cancel", 3),

    /**
     * 退货正在审查中。
     */
    IN_RETURN_REVIEW("In Return Review", 6),

    /**
     * 订单正在处理中。
     */
    PROCESSING("Processing", 7),

    /**
     * 订单已退货。
     */
    RETURNED("Returned", 9),

    /**
     * 其他未分类的状态。
     */
    OTHERS("Others", -1);

    private final String description;
    private final int value;

    /**
     * 构造函数用于初始化每个枚举值的描述和数值。
     *
     * @param description 描述状态的文字。
     * @param value       与状态关联的数值。
     */
    AutonomousOrderStatus(String description, int value) {
        this.description = description;
        this.value = value;
    }

    /**
     * 获取状态的描述。
     *
     * @return 状态描述。
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取状态的数值。
     *
     * @return 状态数值。
     */
    public int getValue() {
        return value;
    }
}

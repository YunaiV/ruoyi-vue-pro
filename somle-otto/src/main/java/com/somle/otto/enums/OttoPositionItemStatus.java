package com.somle.otto.enums;

public enum OttoPositionItemStatus {

    ANNOUNCED("已订购的商品项将为客户保留，但尚未发货。此商品项仍可由市场或客户取消。在此状态下客户的地址不可见。"),
    PROCESSABLE("该商品项已准备好，待履行。"),
    SENT("该商品项已发货，已通过发货接口处理。"),
    RETURNED("该商品项已退货，已通过退货接口处理。"),
    CANCELLED_BY_PARTNER("该商品项已由合作伙伴取消，将不再处理。"),
    CANCELLED_BY_MARKETPLACE("该商品项已由市场取消，将不再处理。如果合作伙伴终止，所有处于“已订购”或“可履行”状态的商品项将被取消。");

    private final String description;

    // 构造方法，用于设置每个枚举常量的描述
    OttoPositionItemStatus(String description) {
        this.description = description;
    }

    // 获取状态的描述
    public String getDescription() {
        return description;
    }

    // 通过名称获取枚举值
    public static OttoPositionItemStatus fromString(String status) {
        for (OttoPositionItemStatus itemStatus : OttoPositionItemStatus.values()) {
            if (itemStatus.name().equalsIgnoreCase(status)) {
                return itemStatus;
            }
        }
        throw new IllegalArgumentException("未找到对应的状态: " + status);
    }
}

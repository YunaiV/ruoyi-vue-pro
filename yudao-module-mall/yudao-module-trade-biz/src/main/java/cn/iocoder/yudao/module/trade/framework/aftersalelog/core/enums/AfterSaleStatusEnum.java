package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.enums;

/**
 * 售后状态的枚举
 *
 * @author 陈賝
 * @since 2023/6/13 13:53
 */
public enum AfterSaleStatusEnum {

    /**
     * 申请中
     */
    APPLY("申请中");

    private final String description;

    AfterSaleStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

}

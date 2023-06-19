package cn.iocoder.yudao.module.trade.enums.aftersale;

/**
 * 售后操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/6/13 13:53
 */
public enum AfterSaleOperateTypeEnum {

    /**
     * 用户申请
     */
    APPLY("用户申请"),
    ;

    private final String description;

    AfterSaleOperateTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

}

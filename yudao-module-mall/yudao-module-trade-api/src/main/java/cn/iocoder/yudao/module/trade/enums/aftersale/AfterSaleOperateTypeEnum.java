package cn.iocoder.yudao.module.trade.enums.aftersale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 售后操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/6/13 13:53
 */
@RequiredArgsConstructor
@Getter
public enum AfterSaleOperateTypeEnum {

    /**
     * 用户申请
     */
    APPLY(0, "用户申请"),
    ;

    // 类型
    private final Integer type;
    // 描述
    private final String description;

    public String description() {
        return description;
    }

}

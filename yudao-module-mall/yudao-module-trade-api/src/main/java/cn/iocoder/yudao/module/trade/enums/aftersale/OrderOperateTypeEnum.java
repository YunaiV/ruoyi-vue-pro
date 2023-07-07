package cn.iocoder.yudao.module.trade.enums.aftersale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/7/6 15:31
 */
@RequiredArgsConstructor
@Getter
public enum OrderOperateTypeEnum {

    /**
     * 用户下单
     */
    ORDER(0, "用户下单"),
    ;

    // 类型
    private final Integer type;
    // 描述
    private final String description;

    public String description() {
        return description;
    }
}

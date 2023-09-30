package cn.iocoder.yudao.module.member.enums.point;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 会员积分的业务类型枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum MemberPointBizTypeEnum implements IntArrayValuable {

    SIGN(1, "签到", "签到获得 {} 积分", true),
    ADMIN(2, "管理员修改", "管理员修改 {} 积分", true),
    ORDER_GIVE(10, "订单奖励", "下单获得 {} 积分", true), // 支付订单时，赠送积分
    ORDER_CANCEL(11, "订单取消", "订单取消，退还 {} 积分", true), // 取消订单时，退回积分
    ORDER_USE(12, "订单使用", "下单使用 {} 积分", false), // 下单时，扣减积分
    AFTER_SALE_REFUND_USED(13, "订单退款", "订单退款，退还 {} 积分", true), // 售后订单成功时，退回积分（对应 ORDER_USE 操作）
    AFTER_SALE_DEDUCT_GIVE(14, "订单退款", "订单退款，扣除赠送的 {} 积分", false), // 售后订单成功时，扣减积分（对应 ORDER_GIVE 操作）
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;
    /**
     * 描述
     */
    private final String description;
    /**
     * 是否为扣减积分
     */
    private final boolean add;

    @Override
    public int[] array() {
        return new int[0];
    }

    public static MemberPointBizTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(MemberPointBizTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}

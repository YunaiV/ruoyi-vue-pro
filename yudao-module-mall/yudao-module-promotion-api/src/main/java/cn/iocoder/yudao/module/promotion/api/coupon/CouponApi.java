package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponValidReqDTO;
import jakarta.validation.Valid;

import java.util.Map;

/**
 * 优惠劵 API 接口
 *
 * @author 芋道源码
 */
public interface CouponApi {

    /**
     * 使用优惠劵
     *
     * @param useReqDTO 使用请求
     */
    void useCoupon(@Valid CouponUseReqDTO useReqDTO);

    /**
     * 退还已使用的优惠券
     *
     * @param id 优惠券编号
     */
    void returnUsedCoupon(Long id);

    /**
     * 校验优惠劵
     *
     * @param validReqDTO 校验请求
     * @return 优惠劵
     */
    CouponRespDTO validateCoupon(@Valid CouponValidReqDTO validReqDTO);

    // TODO @puhui999：可能需要根据 TradeOrderDO 的建议，进行修改；需要返回优惠劵编号
    /**
     * 【管理员】给指定用户批量发送优惠券
     *
     * @param giveCouponsMap  key: 优惠劵编号，value：对应的优惠券数量
     * @param userId      用户编号
     */
    // TODO @puhui999：giveCouponsMap 可能改成 giveCoupons 更合适？优惠劵模版编号、数量
    void takeCouponsByAdmin(Map<Long, Integer> giveCouponsMap, Long userId);

    // TODO @puhui999：可能需要根据 TradeOrderDO 的建议，进行修改 giveCouponsMap 参数
    /**
     * 【管理员】作废指定用户的指定优惠劵
     *
     * @param giveCouponsMap key: 优惠劵编号，value：对应的优惠券数量
     * @param userId         用户编号
     */
    void invalidateCouponsByAdmin(Map<Long, Integer> giveCouponsMap, Long userId);

}

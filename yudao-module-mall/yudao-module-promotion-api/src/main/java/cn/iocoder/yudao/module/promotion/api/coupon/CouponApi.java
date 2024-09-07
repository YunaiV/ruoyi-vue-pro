package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 优惠劵 API 接口
 *
 * @author 芋道源码
 */
public interface CouponApi {

    /**
     * 获得用户的优惠劵列表
     *
     * @param userId 用户编号
     * @param status 优惠劵状态
     * @return 优惠劵列表
     */
    List<CouponRespDTO> getCouponListByUserId(Long userId, Integer status);

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
     * 【管理员】给指定用户批量发送优惠券
     *
     * @param giveCoupons  key: 优惠劵模版编号，value：对应的数量
     * @param userId      用户编号
     * @return 优惠券编号列表
     */
    List<Long> takeCouponsByAdmin(Map<Long, Integer> giveCoupons, Long userId);

    /**
     * 【管理员】作废指定用户的指定优惠劵
     *
     * @param giveCouponIds  赠送的优惠券编号
     * @param userId         用户编号
     */
    void invalidateCouponsByAdmin(List<Long> giveCouponIds, Long userId);

}

package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponValidReqDTO;
import jakarta.validation.Valid;

import java.util.List;

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

    /**
     * 【管理员】给指定用户批量发送优惠券
     *
     * @param templateIds 优惠劵编号的数组
     * @param counts      优惠券数量的数组
     * @param userId      用户编号
     */
    void takeCouponsByAdmin(List<Long> templateIds, List<Integer> counts, Long userId);

}

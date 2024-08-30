package cn.iocoder.yudao.module.promotion.api.coupon;


import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponValidReqDTO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * 优惠劵 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CouponApiImpl implements CouponApi {

    @Resource
    private CouponService couponService;

    @Override
    public void useCoupon(CouponUseReqDTO useReqDTO) {
        couponService.useCoupon(useReqDTO.getId(), useReqDTO.getUserId(),
                useReqDTO.getOrderId());
    }

    @Override
    public void returnUsedCoupon(Long id) {
        couponService.returnUsedCoupon(id);
    }

    @Override
    public CouponRespDTO validateCoupon(CouponValidReqDTO validReqDTO) {
        CouponDO coupon = couponService.validCoupon(validReqDTO.getId(), validReqDTO.getUserId());
        return CouponConvert.INSTANCE.convert(coupon);
    }

    @Override
    public void takeCouponsByAdmin(Map<Long, Integer> giveCouponsMap, Long userId) {
        couponService.takeCouponsByAdmin(giveCouponsMap, userId);
    }

    @Override
    public void invalidateCouponsByAdmin(Map<Long, Integer> giveCouponsMap, Long userId) {
        couponService.invalidateCouponsByAdmin(giveCouponsMap, userId);
    }

}

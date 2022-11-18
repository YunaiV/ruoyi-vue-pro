package cn.iocoder.yudao.module.promotion.api.coupon;


import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 优惠劵 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class CouponApiImpl implements CouponApi {

    @Resource
    private CouponService couponService;

    @Override
    public void useCoupon(CouponUseReqDTO useReqDTO) {
        couponService.useCoupon(useReqDTO.getId(), useReqDTO.getUserId(),
                useReqDTO.getOrderId());
    }

}

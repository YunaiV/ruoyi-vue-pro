package cn.iocoder.yudao.module.market.service.coupon;

import cn.iocoder.yudao.module.market.dal.dataobject.coupon.CouponDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 优惠劵 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CouponServiceImpl implements CouponService {

    // TODO 芋艿：待实现
    @Override
    public CouponDO validCoupon(Long id, Long userId) {
        return null;
    }
}

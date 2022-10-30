package cn.iocoder.yudao.module.market.service.coupon;

import cn.iocoder.yudao.module.market.dal.dataobject.coupon.CouponDO;

/**
 * 优惠劵 Service 接口
 *
 * @author 芋道源码
 */
public interface CouponService {

    /**
     * 校验优惠劵，包括状态、有限期
     *
     * 1. 如果校验通过，则返回优惠劵信息
     * 2. 如果校验不通过，则直接抛出业务异常
     *
     * @param id 优惠劵编号
     * @param userId 用户编号
     * @return 优惠劵信息
     */
    CouponDO validCoupon(Long id, Long userId);

}

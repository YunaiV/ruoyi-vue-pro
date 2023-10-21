package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponTemplateRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 优惠劵模版 API 接口
 *
 * @author HUIHUI
 */
public interface CouponTemplateApi {

    /**
     * 获得优惠券模版的精简信息列表
     *
     * @param ids 优惠券模版编号
     * @return 优惠券模版的精简信息列表
     */
    List<CouponTemplateRespDTO> getCouponTemplateListByIds(Collection<Long> ids);

}

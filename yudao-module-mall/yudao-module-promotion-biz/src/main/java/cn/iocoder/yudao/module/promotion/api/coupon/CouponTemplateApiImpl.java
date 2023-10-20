package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponTemplateRespDTO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponTemplateConvert;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 优惠劵模版 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
public class CouponTemplateApiImpl implements CouponTemplateApi {

    @Resource
    private CouponTemplateService couponTemplateService;

    @Override
    public List<CouponTemplateRespDTO> getCouponTemplateListByIds(Collection<Long> ids) {
        return CouponTemplateConvert.INSTANCE.convertList(couponTemplateService.getCouponTemplateListByIds(ids));
    }

}

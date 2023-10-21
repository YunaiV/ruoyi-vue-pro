package cn.iocoder.yudao.module.promotion.api.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponTemplateRespDTO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponTemplateConvert;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
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
        if (CollUtil.isEmpty(ids)) { // 防御一下
            return Collections.emptyList();
        }
        return CouponTemplateConvert.INSTANCE.convertList(couponTemplateService.getCouponTemplateListByIds(ids));
    }

}

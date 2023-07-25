package cn.iocoder.yudao.module.promotion.service.price;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.promotion.convert.price.PriceConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_VALID_TIME_NOT_NOW;

/**
 * 价格计算 Service 实现类
 *
 * 优惠计算顺序：min(限时折扣, 会员折扣) > 满减送 > 优惠券。
 * 参考文档：
 * 1. <a href="https://help.youzan.com/displaylist/detail_4_4-1-60384">有赞文档：限时折扣、满减送、优惠券哪个优先计算？</a>
 *
 * TODO 芋艿：进一步完善
 * 1. 限时折扣：指定金额、减免金额、折扣
 * 2. 满减送：循环、折扣
 * 3. 优惠劵：待定
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PriceServiceImpl implements PriceService {

    @Resource
    private CouponService couponService;

    @Override
    public List<CouponMeetRespDTO> getMeetCouponList(PriceCalculateReqDTO calculateReqDTO) {
        // 先计算一轮价格
//        PriceCalculateRespDTO priceCalculate = calculatePrice(calculateReqDTO);
        PriceCalculateRespDTO priceCalculate = null;

        // 获得用户的待使用优惠劵
        List<CouponDO> couponList = couponService.getCouponList(calculateReqDTO.getUserId(), CouponStatusEnum.UNUSED.getStatus());
        if (CollUtil.isEmpty(couponList)) {
            return Collections.emptyList();
        }

        // 获得优惠劵的匹配信息
        return CollectionUtils.convertList(couponList, coupon -> {
            CouponMeetRespDTO couponMeetRespDTO = PriceConvert.INSTANCE.convert(coupon);
            try {
                // 校验优惠劵
                couponService.validCoupon(coupon);

                // 获得匹配的商品 SKU 数组
                // TODO 芋艿：后续处理
//                List<PriceCalculateRespDTO.OrderItem> orderItems = getMatchCouponOrderItems(priceCalculate, coupon);
                List<PriceCalculateRespDTO.OrderItem> orderItems = null;
                if (CollUtil.isEmpty(orderItems)) {
                    return couponMeetRespDTO.setMeet(false).setMeetTip("所结算商品没有符合条件的商品");
                }

                // 计算是否满足优惠劵的使用金额
                Integer originPrice = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum);
                assert originPrice != null;
                if (originPrice < coupon.getUsePrice()) {
                    return couponMeetRespDTO.setMeet(false)
//                            .setMeetTip(String.format("差 %s 元可用优惠劵", formatPrice(coupon.getUsePrice() - originPrice)));
                            .setMeetTip("所结算的商品中未满足使用的金额");
                }
            } catch (ServiceException serviceException) {
                couponMeetRespDTO.setMeet(false);
                if (serviceException.getCode().equals(COUPON_VALID_TIME_NOT_NOW.getCode())) {
                    couponMeetRespDTO.setMeetTip("优惠劵未到使用时间");
                } else {
                    log.error("[getMeetCouponList][calculateReqDTO({}) 获得优惠劵匹配信息异常]", calculateReqDTO, serviceException);
                    couponMeetRespDTO.setMeetTip("优惠劵不满足使用条件");
                }
                return couponMeetRespDTO;
            }
            // 满足
            return couponMeetRespDTO.setMeet(true);
        });
    }

}

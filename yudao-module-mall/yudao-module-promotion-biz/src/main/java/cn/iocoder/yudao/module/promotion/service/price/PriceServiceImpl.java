package cn.iocoder.yudao.module.promotion.service.price;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.promotion.convert.price.PriceConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.enums.common.*;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import cn.iocoder.yudao.module.promotion.service.discount.DiscountActivityService;
import cn.iocoder.yudao.module.promotion.service.discount.bo.DiscountProductDetailBO;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;

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
    private DiscountActivityService discountService;
    @Resource
    private RewardActivityService rewardActivityService;
    @Resource
    private CouponService couponService;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO) {
        // 获得商品 SKU 数组
        List<ProductSkuRespDTO> skuList = checkSkus(calculateReqDTO);
        // 初始化 PriceCalculateRespDTO 对象
        PriceCalculateRespDTO priceCalculate = PriceConvert.INSTANCE.convert(calculateReqDTO, skuList);

        // 计算商品级别的价格
        calculatePriceForSkuLevel(calculateReqDTO.getUserId(), priceCalculate);
        // 计算订单级别的价格
        calculatePriceForOrderLevel(calculateReqDTO.getUserId(), priceCalculate);
        // 计算优惠劵级别的价格
        calculatePriceForCouponLevel(calculateReqDTO.getUserId(), calculateReqDTO.getCouponId(), priceCalculate);

        // 如果最终支付金额小于等于 0，则抛出业务异常
        if (priceCalculate.getOrder().getPayPrice() <= 0) {
            log.error("[calculatePrice][价格计算不正确，请求 calculateReqDTO({})，结果 priceCalculate({})]",
                    calculateReqDTO, priceCalculate);
            throw exception(PRICE_CALCULATE_PAY_PRICE_ILLEGAL);
        }
        return priceCalculate;
    }

    @Override
    public List<CouponMeetRespDTO> getMeetCouponList(PriceCalculateReqDTO calculateReqDTO) {
        // 先计算一轮价格
        PriceCalculateRespDTO priceCalculate = calculatePrice(calculateReqDTO);

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
                List<PriceCalculateRespDTO.OrderItem> orderItems = getMatchCouponOrderItems(priceCalculate, coupon);
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

    private List<ProductSkuRespDTO> checkSkus(PriceCalculateReqDTO calculateReqDTO) {
        // 获得商品 SKU 数组
        Map<Long, Integer> skuIdCountMap = CollectionUtils.convertMap(calculateReqDTO.getItems(),
                PriceCalculateReqDTO.Item::getSkuId, PriceCalculateReqDTO.Item::getCount);
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIdCountMap.keySet());

        // 校验商品 SKU
        skus.forEach(sku -> {
            Integer count = skuIdCountMap.get(sku.getId());
            if (count == null) {
                throw exception(SKU_NOT_EXISTS);
            }
            // 不校验库存不足，避免购物车场景，商品无货的情况
        });
        return skus;
    }

    // ========== 计算商品级别的价格 ==========

    /**
     * 计算商品级别的价格，例如说：
     * 1. 会员折扣
     * 2. 限时折扣 {@link cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO}
     *
     * 其中，会员折扣、限时折扣取最低价
     *
     * @param userId 用户编号
     * @param priceCalculate 价格计算的结果
     */
    private void calculatePriceForSkuLevel(Long userId, PriceCalculateRespDTO priceCalculate) {
        // 获取 SKU 级别的所有优惠信息
        Supplier<Double> memberDiscountPercentSupplier = getMemberDiscountPercentSupplier(userId);
        Map<Long, DiscountProductDetailBO> discountProducts = discountService.getMatchDiscountProducts(
                convertSet(priceCalculate.getOrder().getItems(), PriceCalculateRespDTO.OrderItem::getSkuId));

        // 处理每个 SKU 的优惠
        priceCalculate.getOrder().getItems().forEach(orderItem -> {
            // 获取该 SKU 的优惠信息
            Double memberDiscountPercent = memberDiscountPercentSupplier.get();
            DiscountProductDetailBO discountProduct = discountProducts.get(orderItem.getSkuId());
            if (memberDiscountPercent == null && discountProduct == null) {
                return;
            }
            // 计算价格，判断选择哪个折扣
            Integer memberPrice = memberDiscountPercent != null ? (int) (orderItem.getPayPrice() * memberDiscountPercent / 100) : null;
            Integer promotionPrice = discountProduct != null ? getDiscountProductPrice(discountProduct, orderItem) : null;
            if (memberPrice == null) {
                calculatePriceByDiscountActivity(priceCalculate, orderItem, discountProduct, promotionPrice);
            } else if (promotionPrice == null) {
                calculatePriceByMemberDiscount(priceCalculate, orderItem, memberPrice);
            } else if (memberPrice < promotionPrice) {
                calculatePriceByDiscountActivity(priceCalculate, orderItem, discountProduct, promotionPrice);
            } else {
                calculatePriceByMemberDiscount(priceCalculate, orderItem, memberPrice);
            }
        });
    }

    private Integer getDiscountProductPrice(DiscountProductDetailBO discountProduct,
                                            PriceCalculateRespDTO.OrderItem orderItem) {
        Integer price = orderItem.getPayPrice();
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(discountProduct.getDiscountType())) { // 减价
            price -= discountProduct.getDiscountPrice() * orderItem.getCount();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(discountProduct.getDiscountType())) { // 打折
            price = price * discountProduct.getDiscountPercent() / 100;
        } else {
            throw new IllegalArgumentException(String.format("优惠活动的商品(%s) 的优惠类型不正确", discountProduct));
        }
        return price;
    }

    private void calculatePriceByMemberDiscount(PriceCalculateRespDTO priceCalculate, PriceCalculateRespDTO.OrderItem orderItem,
                                                Integer memberPrice) {
        // 记录优惠明细
        addPromotion(priceCalculate, orderItem, null, PromotionTypeEnum.MEMBER.getName(),
                PromotionTypeEnum.MEMBER.getType(), PromotionLevelEnum.SKU.getLevel(), memberPrice,
                true, StrUtil.format("会员折扣：省 {} 元", formatPrice(orderItem.getPayPrice() - memberPrice)));
        // 修改 SKU 的优惠
        modifyOrderItemPayPrice(orderItem, memberPrice, priceCalculate);
    }

    private void calculatePriceByDiscountActivity(PriceCalculateRespDTO priceCalculate, PriceCalculateRespDTO.OrderItem orderItem,
                                                  DiscountProductDetailBO discountProduct, Integer promotionPrice) {
        // 记录优惠明细
        addPromotion(priceCalculate, orderItem, discountProduct.getActivityId(), discountProduct.getActivityName(),
                PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(), PromotionLevelEnum.SKU.getLevel(), promotionPrice,
                true, StrUtil.format("限时折扣：省 {} 元", formatPrice(orderItem.getPayPrice() - promotionPrice)));
        // 修改 SKU 的优惠
        modifyOrderItemPayPrice(orderItem, promotionPrice, priceCalculate);
    }

    // TODO 芋艿：提前实现
    private Supplier<Double> getMemberDiscountPercentSupplier(Long userId) {
        return Suppliers.memoize(() -> {
            if (userId == 1) {
                return 90d;
            }
            if (userId == 2) {
                return 80d;
            }
            return null; // 无优惠
        });
    }

    // ========== 计算商品级别的价格 ==========

    /**
     * 计算订单级别的价格，例如说：
     * 1. 满减送 {@link cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO}
     *
     * @param userId 用户编号
     * @param priceCalculate 价格计算的结果
     */
    @SuppressWarnings("unused")
    private void calculatePriceForOrderLevel(Long userId, PriceCalculateRespDTO priceCalculate) {
        // 获取 SKU 级别的所有优惠信息
        Set<Long> spuIds = convertSet(priceCalculate.getOrder().getItems(), PriceCalculateRespDTO.OrderItem::getSpuId);
        Map<RewardActivityDO, Set<Long>> rewardActivities = rewardActivityService.getMatchRewardActivities(spuIds);

        // 处理满减送活动
        if (CollUtil.isNotEmpty(rewardActivities)) {
            rewardActivities.forEach((rewardActivity, activitySpuIds) -> {
                List<PriceCalculateRespDTO.OrderItem> orderItems = CollectionUtils.filterList(priceCalculate.getOrder().getItems(),
                        orderItem -> CollUtil.contains(activitySpuIds, orderItem.getSpuId()));
                calculatePriceByRewardActivity(priceCalculate, orderItems, rewardActivity);
            });
        }
    }

    private void calculatePriceByRewardActivity(PriceCalculateRespDTO priceCalculate, List<PriceCalculateRespDTO.OrderItem> orderItems,
                                                RewardActivityDO rewardActivity) {
        // 获得最大匹配的满减送活动的规格
        RewardActivityDO.Rule rule = getLastMatchRewardActivityRule(rewardActivity, orderItems);
        if (rule == null) {
            // 获取不到的情况下，记录不满足的优惠明细
            addNotMeetPromotion(priceCalculate, orderItems, rewardActivity.getId(), rewardActivity.getName(),
                    PromotionTypeEnum.REWARD_ACTIVITY.getType(), PromotionLevelEnum.ORDER.getLevel(),
                    getRewardActivityNotMeetTip(rewardActivity));
            return;
        }

        // 分摊金额
        List<Integer> discountPartPrices = dividePrice(orderItems, rule.getDiscountPrice());
        // 记录优惠明细
        addPromotion(priceCalculate, orderItems, rewardActivity.getId(), rewardActivity.getName(),
                PromotionTypeEnum.REWARD_ACTIVITY.getType(), PromotionLevelEnum.ORDER.getLevel(), discountPartPrices,
                true, StrUtil.format("满减送：省 {} 元", formatPrice(rule.getDiscountPrice())));
        // 修改 SKU 的分摊
        for (int i = 0; i < orderItems.size(); i++) {
            modifyOrderItemOrderPartPriceFromDiscountPrice(orderItems.get(i), discountPartPrices.get(i), priceCalculate);
        }
    }

    /**
     * 获得最大匹配的满减送活动的规格
     *
     * @param rewardActivity 满减送活动
     * @param orderItems 商品项
     * @return 匹配的活动规格
     */
    private RewardActivityDO.Rule getLastMatchRewardActivityRule(RewardActivityDO rewardActivity,
                                                                 List<PriceCalculateRespDTO.OrderItem> orderItems) {
        Integer count = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getCount, Integer::sum);
        // price 的计算逻辑，使用 orderDividePrice 的原因，主要考虑分摊后，这个才是该 SKU 当前真实的支付总价
        Integer price = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum);
        assert count != null && price != null;
        for (int i = rewardActivity.getRules().size() - 1; i >= 0; i--) {
            RewardActivityDO.Rule rule = rewardActivity.getRules().get(i);
            if (PromotionConditionTypeEnum.PRICE.getType().equals(rewardActivity.getConditionType())
                    && price >= rule.getLimit()) {
                return rule;
            }
            if (PromotionConditionTypeEnum.COUNT.getType().equals(rewardActivity.getConditionType())
                    && count >= rule.getLimit()) {
                return rule;
            }
        }
        return null;
    }

    /**
     * 获得满减送活动部匹配时的提示
     *
     * @param rewardActivity 满减送活动
     * @return 提示
     */
    private String getRewardActivityNotMeetTip(RewardActivityDO rewardActivity) {
        return "TODO"; // TODO 芋艿：后面再想想
    }

    // ========== 计算优惠劵级别的价格 ==========

    private void calculatePriceForCouponLevel(Long userId, Long couponId, PriceCalculateRespDTO priceCalculate) {
        // 校验优惠劵
        if (couponId == null) {
            return;
        }
        CouponDO coupon = couponService.validCoupon(couponId, userId);

        // 获得匹配的商品 SKU 数组
        List<PriceCalculateRespDTO.OrderItem> orderItems = getMatchCouponOrderItems(priceCalculate, coupon);
        if (CollUtil.isEmpty(orderItems)) {
            throw exception(COUPON_NO_MATCH_SPU);
        }

        // 计算是否满足优惠劵的使用金额
        Integer originPrice = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum);
        assert originPrice != null;
        if (originPrice < coupon.getUsePrice()) {
            throw exception(COUPON_NO_MATCH_MIN_PRICE);
        }

        // 计算可以优惠的金额
        priceCalculate.getOrder().setCouponId(couponId);
        Integer couponPrice = getCouponPrice(coupon, originPrice);
        // 分摊金额
        List<Integer> couponPartPrices = dividePrice(orderItems, couponPrice);
        // 记录优惠明细
        addPromotion(priceCalculate, orderItems, coupon.getId(), coupon.getName(),
                PromotionTypeEnum.COUPON.getType(), PromotionLevelEnum.COUPON.getLevel(), couponPartPrices,
                true, StrUtil.format("优惠劵：省 {} 元", formatPrice(couponPrice)));
        // 修改 SKU 的分摊
        for (int i = 0; i < orderItems.size(); i++) {
            modifyOrderItemOrderPartPriceFromCouponPrice(orderItems.get(i), couponPartPrices.get(i), priceCalculate);
        }
    }

    private List<PriceCalculateRespDTO.OrderItem> getMatchCouponOrderItems(PriceCalculateRespDTO priceCalculate,
                                                                           CouponDO coupon) {
        if (PromotionProductScopeEnum.ALL.getScope().equals(coupon.getProductScope())) {
            return priceCalculate.getOrder().getItems();
        }
        return CollectionUtils.filterList(priceCalculate.getOrder().getItems(),
                orderItem -> coupon.getProductSpuIds().contains(orderItem.getSpuId()));
    }

    private Integer getCouponPrice(CouponDO coupon, Integer originPrice) {
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(coupon.getDiscountType())) { // 减价
            return coupon.getDiscountPrice();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(coupon.getDiscountType())) { // 打折
            int couponPrice = originPrice * coupon.getDiscountPercent() / 100;
            return coupon.getDiscountLimitPrice() == null ? couponPrice
                    : Math.min(couponPrice, coupon.getDiscountLimitPrice()); // 优惠上限
        }
        throw new IllegalArgumentException(String.format("优惠劵(%s) 的优惠类型不正确", coupon));
    }

    // ========== 其它相对通用的方法 ==========

    /**
     * 添加单个 OrderItem 的营销明细
     *
     * @param priceCalculate 价格计算结果
     * @param orderItem 单个订单商品 SKU
     * @param id 营销编号
     * @param name 营销名字
     * @param type 营销类型
     * @param level 营销级别
     * @param newPayPrice 新的单实付金额（总）
     * @param meet 是否满足优惠条件
     * @param meetTip 满足条件的提示
     */
    private void addPromotion(PriceCalculateRespDTO priceCalculate, PriceCalculateRespDTO.OrderItem orderItem,
                              Long id, String name, Integer type, Integer level,
                              Integer newPayPrice, Boolean meet, String meetTip) {
        // 创建营销明细 Item
        // TODO 芋艿：orderItem.getPayPrice() 要不要改成 orderDividePrice；同时，newPayPrice 要不要改成直接传递 discountPrice
        PriceCalculateRespDTO.PromotionItem promotionItem = new PriceCalculateRespDTO.PromotionItem().setSkuId(orderItem.getSkuId())
                .setOriginalPrice(orderItem.getPayPrice()).setDiscountPrice(orderItem.getPayPrice() - newPayPrice);
        // 创建营销明细
        PriceCalculateRespDTO.Promotion promotion = new PriceCalculateRespDTO.Promotion()
                .setId(id).setName(name).setType(type).setLevel(level)
                .setOriginalPrice(promotionItem.getOriginalPrice()).setDiscountPrice(promotionItem.getDiscountPrice())
                .setItems(singletonList(promotionItem)).setMeet(meet).setMeetTip(meetTip);
        priceCalculate.getPromotions().add(promotion);
    }

    /**
     * 添加多个 OrderItem 的营销明细
     *
     * @param priceCalculate 价格计算结果
     * @param orderItems 多个订单商品 SKU
     * @param id 营销编号
     * @param name 营销名字
     * @param type 营销类型
     * @param level 营销级别
     * @param discountPrices 多个订单商品 SKU 的优惠价格（总），和 orderItems 一一对应
     * @param meet 是否满足优惠条件
     * @param meetTip 满足条件的提示
     */
    private void addPromotion(PriceCalculateRespDTO priceCalculate, List<PriceCalculateRespDTO.OrderItem> orderItems,
                              Long id, String name, Integer type, Integer level,
                              List<Integer> discountPrices, Boolean meet, String meetTip) {
        // 创建营销明细 Item
        List<PriceCalculateRespDTO.PromotionItem> promotionItems = new ArrayList<>(discountPrices.size());
        for (int i = 0; i < orderItems.size(); i++) {
            PriceCalculateRespDTO.OrderItem orderItem = orderItems.get(i);
            promotionItems.add(new PriceCalculateRespDTO.PromotionItem().setSkuId(orderItem.getSkuId())
                    .setOriginalPrice(orderItem.getPayPrice()).setDiscountPrice(discountPrices.get(i)));
        }
        // 创建营销明细
        PriceCalculateRespDTO.Promotion promotion = new PriceCalculateRespDTO.Promotion()
                .setId(id).setName(name).setType(type).setLevel(level)
                .setOriginalPrice(getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum))
                .setDiscountPrice(getSumValue(discountPrices, value -> value, Integer::sum))
                .setItems(promotionItems).setMeet(meet).setMeetTip(meetTip);
        priceCalculate.getPromotions().add(promotion);
    }

    private void addNotMeetPromotion(PriceCalculateRespDTO priceCalculate, List<PriceCalculateRespDTO.OrderItem> orderItems,
                                     Long id, String name, Integer type, Integer level, String meetTip) {
        // 创建营销明细 Item
        List<PriceCalculateRespDTO.PromotionItem> promotionItems = CollectionUtils.convertList(orderItems,
                orderItem -> new PriceCalculateRespDTO.PromotionItem().setSkuId(orderItem.getSkuId())
                        .setOriginalPrice(orderItem.getOrderDividePrice()).setDiscountPrice(0));
        // 创建营销明细
        Integer originalPrice = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum);
        PriceCalculateRespDTO.Promotion promotion = new PriceCalculateRespDTO.Promotion()
                .setId(id).setName(name).setType(type).setLevel(level)
                .setOriginalPrice(originalPrice).setDiscountPrice(0)
                .setItems(promotionItems).setMeet(false).setMeetTip(meetTip);
        priceCalculate.getPromotions().add(promotion);
    }

    /**
     * 修改 OrderItem 的 payPrice 价格，同时会修改 Order 的 payPrice 价格
     *
     * @param orderItem 订单商品 SKU
     * @param newPayPrice 新的 payPrice 价格
     * @param priceCalculate 价格计算结果
     */
    private void modifyOrderItemPayPrice(PriceCalculateRespDTO.OrderItem orderItem, Integer newPayPrice,
                                         PriceCalculateRespDTO priceCalculate) {
        // diffPayPrice 等于额外增加的商品级的优惠
        int diffPayPrice = orderItem.getPayPrice() - newPayPrice;
        // 设置 OrderItem 价格相关字段
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + diffPayPrice);
        orderItem.setPayPrice(newPayPrice);
        orderItem.setOrderDividePrice(orderItem.getPayPrice() - orderItem.getOrderPartPrice());
        // 设置 Order 相关相关字段
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        order.setPayPrice(order.getPayPrice() - diffPayPrice);
        order.setOrderPrice(order.getOrderPrice() - diffPayPrice);
    }

    /**
     * 修改 OrderItem 的 orderPartPrice 价格，同时会修改 Order 的 discountPrice 价格
     *
     * 本质：分摊 Order 的 discountPrice 价格，到对应的 OrderItem 的 orderPartPrice 价格中
     *
     * @param orderItem 订单商品 SKU
     * @param addOrderPartPrice 新增的 discountPrice 价格
     * @param priceCalculate 价格计算结果
     */
    private void modifyOrderItemOrderPartPriceFromDiscountPrice(PriceCalculateRespDTO.OrderItem orderItem, Integer addOrderPartPrice,
                                                                PriceCalculateRespDTO priceCalculate) {
        // 设置 OrderItem 价格相关字段
        orderItem.setOrderPartPrice(orderItem.getOrderPartPrice() + addOrderPartPrice);
        orderItem.setOrderDividePrice(orderItem.getPayPrice() - orderItem.getOrderPartPrice());
        // 设置 Order 相关相关字段
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        order.setDiscountPrice(order.getDiscountPrice() + addOrderPartPrice);
        order.setPayPrice(order.getPayPrice() - addOrderPartPrice);
    }

    /**
     * 修改 OrderItem 的 orderPartPrice 价格，同时会修改 Order 的 couponPrice 价格
     *
     * 本质：分摊 Order 的 couponPrice 价格，到对应的 OrderItem 的 orderPartPrice 价格中
     *
     * @param orderItem 订单商品 SKU
     * @param addOrderPartPrice 新增的 couponPrice 价格
     * @param priceCalculate 价格计算结果
     */
    private void modifyOrderItemOrderPartPriceFromCouponPrice(PriceCalculateRespDTO.OrderItem orderItem, Integer addOrderPartPrice,
                                                              PriceCalculateRespDTO priceCalculate) {
        // 设置 OrderItem 价格相关字段
        orderItem.setOrderPartPrice(orderItem.getOrderPartPrice() + addOrderPartPrice);
        orderItem.setOrderDividePrice(orderItem.getPayPrice() - orderItem.getOrderPartPrice());
        // 设置 Order 相关相关字段
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        order.setCouponPrice(order.getCouponPrice() + addOrderPartPrice);
        order.setPayPrice(order.getPayPrice() - addOrderPartPrice);
    }

    private List<Integer> dividePrice(List<PriceCalculateRespDTO.OrderItem> orderItems, Integer price) {
        List<Integer> prices = new ArrayList<>(orderItems.size());
        Integer total = getSumValue(orderItems, PriceCalculateRespDTO.OrderItem::getOrderDividePrice, Integer::sum);
        assert total != null;
        int remainPrice = price;
        // 遍历每一个，进行分摊
        for (int i = 0; i < orderItems.size(); i++) {
            PriceCalculateRespDTO.OrderItem orderItem = orderItems.get(i);
            int partPrice;
            if (i < orderItems.size() - 1) { // 减一的原因，是因为拆分时，如果按照比例，可能会出现.所以最后一个，使用反减
                partPrice = (int) (price * (1.0D * orderItem.getOrderDividePrice() / total));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            Assert.isTrue(partPrice > 0, "分摊金额必须大于 0");
            prices.add(partPrice);
        }
        return prices;
    }

    private String formatPrice(Integer price) {
        return String.format("%.2f", price / 100d);
    }

}

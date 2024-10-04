package cn.iocoder.yudao.module.trade.service.price;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.discount.DiscountActivityApi;
import cn.iocoder.yudao.module.promotion.api.discount.dto.DiscountProductRespDTO;
import cn.iocoder.yudao.module.promotion.api.reward.RewardActivityApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeProductSettlementRespVO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradeDiscountActivityPriceCalculator;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculator;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_PAY_PRICE_ILLEGAL;

/**
 * 价格计算 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class TradePriceServiceImpl implements TradePriceService {

    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private DiscountActivityApi discountActivityApi;
    @Resource
    private RewardActivityApi rewardActivityApi;

    @Resource
    private List<TradePriceCalculator> priceCalculators;

    @Resource
    private TradeDiscountActivityPriceCalculator discountActivityPriceCalculator;

    @Override
    public TradePriceCalculateRespBO calculateOrderPrice(TradePriceCalculateReqBO calculateReqBO) {
        // 1.1 获得商品 SKU 数组
        List<ProductSkuRespDTO> skuList = checkSkuList(calculateReqBO);
        // 1.2 获得商品 SPU 数组
        List<ProductSpuRespDTO> spuList = checkSpuList(skuList);

        // 2.1 计算价格
        TradePriceCalculateRespBO calculateRespBO = TradePriceCalculatorHelper
                .buildCalculateResp(calculateReqBO, spuList, skuList);
        priceCalculators.forEach(calculator -> calculator.calculate(calculateReqBO, calculateRespBO));
        // 2.2  如果最终支付金额小于等于 0，则抛出业务异常
        if (calculateReqBO.getPointActivityId() == null // 积分订单，允许支付金额为 0
                && calculateRespBO.getPrice().getPayPrice() <= 0) {
            log.error("[calculatePrice][价格计算不正确，请求 calculateReqDTO({})，结果 priceCalculate({})]",
                    calculateReqBO, calculateRespBO);
            throw exception(PRICE_CALCULATE_PAY_PRICE_ILLEGAL);
        }
        return calculateRespBO;
    }

    private List<ProductSkuRespDTO> checkSkuList(TradePriceCalculateReqBO reqBO) {
        // 获得商品 SKU 数组
        Map<Long, Integer> skuIdCountMap = convertMap(reqBO.getItems(),
                TradePriceCalculateReqBO.Item::getSkuId, TradePriceCalculateReqBO.Item::getCount);
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIdCountMap.keySet());

        // 校验商品 SKU
        skus.forEach(sku -> {
            Integer count = skuIdCountMap.get(sku.getId());
            if (count == null) {
                throw exception(SKU_NOT_EXISTS);
            }
            if (count > sku.getStock()) {
                throw exception(SKU_STOCK_NOT_ENOUGH);
            }
        });
        return skus;
    }

    private List<ProductSpuRespDTO> checkSpuList(List<ProductSkuRespDTO> skuList) {
        // 获得商品 SPU 数组
        return productSpuApi.validateSpuList(convertSet(skuList, ProductSkuRespDTO::getSpuId));
    }

    @Override
    public List<AppTradeProductSettlementRespVO> calculateProductPrice(Long userId, List<Long> spuIds) {
        // 1.1 获得 SPU 与 SKU 的映射
        List<ProductSkuRespDTO> allSkuList = productSkuApi.getSkuListBySpuId(spuIds);
        Map<Long, List<ProductSkuRespDTO>> spuIdAndSkuListMap = convertMultiMap(allSkuList, ProductSkuRespDTO::getSpuId);
        // 1.2 获得会员等级
        MemberLevelRespDTO level = discountActivityPriceCalculator.getMemberLevel(userId);
        // 1.3 获得限时折扣活动
        Map<Long, DiscountProductRespDTO> skuIdAndDiscountMap = convertMap(
                discountActivityApi.getMatchDiscountProductListBySkuIds(convertSet(allSkuList, ProductSkuRespDTO::getId)),
                DiscountProductRespDTO::getSkuId);
        // 1.4 获得满减送活动
       List<RewardActivityMatchRespDTO> rewardActivityMap = rewardActivityApi.getMatchRewardActivityListBySpuIds(spuIds);

        // 2. 价格计算
        return convertList(spuIds, spuId -> {
            AppTradeProductSettlementRespVO spuVO = new AppTradeProductSettlementRespVO().setSpuId(spuId);
            // 2.1 优惠价格
            List<ProductSkuRespDTO> skuList = spuIdAndSkuListMap.get(spuId);
            List<AppTradeProductSettlementRespVO.Sku> skuVOList = convertList(skuList, sku -> {
                AppTradeProductSettlementRespVO.Sku skuVO = new AppTradeProductSettlementRespVO.Sku()
                        .setId(sku.getId()).setPromotionPrice(sku.getPrice());
                TradePriceCalculateRespBO.OrderItem orderItem = new TradePriceCalculateRespBO.OrderItem()
                        .setPayPrice(sku.getPrice()).setCount(1);
                // 计算限时折扣的优惠价格
                DiscountProductRespDTO discountProduct = skuIdAndDiscountMap.get(sku.getId());
                Integer discountPrice = discountActivityPriceCalculator.calculateActivityPrice(discountProduct, orderItem);
                // 计算 VIP 优惠金额
                Integer vipPrice = discountActivityPriceCalculator.calculateVipPrice(level, orderItem);
                if (discountPrice <= 0 && vipPrice <= 0) {
                    return skuVO;
                }
                // 选择一个大的优惠
                if (discountPrice > vipPrice) {
                    return skuVO.setPromotionPrice(sku.getPrice() - discountPrice)
                            .setPromotionType(PromotionTypeEnum.DISCOUNT_ACTIVITY.getType())
                            .setPromotionId(discountProduct.getId()).setPromotionEndTime(discountProduct.getActivityEndTime());
                } else {
                    return skuVO.setPromotionPrice(sku.getPrice() - vipPrice)
                            .setPromotionType(PromotionTypeEnum.MEMBER_LEVEL.getType());
                }
            });
            spuVO.setSkus(skuVOList);
            // 2.2 满减送活动
            RewardActivityMatchRespDTO rewardActivity = CollUtil.findOne(rewardActivityMap,
                    activity -> CollUtil.contains(activity.getSpuIds(), spuId));
            spuVO.setRewardActivity(BeanUtils.toBean(rewardActivity, AppTradeProductSettlementRespVO.RewardActivity.class));
            return spuVO;
        });
    }

}

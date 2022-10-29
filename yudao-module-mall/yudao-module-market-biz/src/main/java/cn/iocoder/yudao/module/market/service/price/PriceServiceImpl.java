package cn.iocoder.yudao.module.market.service.price;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.market.convert.price.PriceConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.discount.DiscountProductDO;
import cn.iocoder.yudao.module.market.enums.common.PromotionLevelEnum;
import cn.iocoder.yudao.module.market.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.market.service.discount.DiscountService;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import com.google.common.base.Suppliers;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static java.util.Collections.singletonList;

/**
 * 价格计算 Service 实现类
 *
 * 优惠计算顺序：min(限时折扣, 会员折扣) > 满减送 > 优惠券。
 * 参考文档：
 * 1. <a href="https://help.youzan.com/displaylist/detail_4_4-1-60384">有赞文档：限时折扣、满减送、优惠券哪个优先计算？</a>
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PriceServiceImpl implements PriceService {

    @Resource
    private DiscountService discountService;

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
        // 计算【满减送】促销 TODO 待实现
        // 计算【优惠劵】促销 TODO 待实现
        return priceCalculate;
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

    /**
     * 计算商品级别的价格，例如说：
     * 1. 会员折扣
     * 2. 限时折扣
     *
     * 其中，会员折扣、限时折扣取最低价
     *
     * @param userId 用户编号
     * @param priceCalculate 价格计算的结果
     */
    private void calculatePriceForSkuLevel(Long userId, PriceCalculateRespDTO priceCalculate) {
        // 获取 SKU 级别的所有优惠信息
        Supplier<Double> memberDiscountSupplier = getMemberDiscountSupplier(userId);
        Map<Long, DiscountProductDO> discountProducts = discountService.getMatchDiscountProducts(
                convertSet(priceCalculate.getOrder().getItems(), PriceCalculateRespDTO.OrderItem::getSkuId));

        // 处理每个 SKU 的优惠
        priceCalculate.getOrder().getItems().forEach(orderItem -> {
            // 获取该 SKU 的优惠信息
            Double memberDiscount = memberDiscountSupplier.get();
            DiscountProductDO discountProduct = discountProducts.get(orderItem.getSkuId());
            if (discountProduct != null // 假设优惠价格更贵，则认为没优惠
                    && discountProduct.getPromotionPrice() >= orderItem.getOriginalUnitPrice()) {
                discountProduct = null;
            }
            if (memberDiscount == null && discountProduct == null) {
                return;
            }
            // 计算价格，判断选择哪个折扣
            Integer memberPrice = memberDiscount != null ? (int) (orderItem.getPayPrice() * memberDiscount / 100) : null;
            Integer promotionPrice = discountProduct != null ? discountProduct.getPromotionPrice() * orderItem.getCount() : null;
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
                                                  DiscountProductDO discountProduct, Integer promotionPrice) {
        // 记录优惠明细
        addPromotion(priceCalculate, orderItem, discountProduct.getActivityId(), discountProduct.getActivityName(),
                PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(), PromotionLevelEnum.SKU.getLevel(), promotionPrice,
                true, StrUtil.format("限时折扣：省 {} 元", formatPrice(orderItem.getPayPrice() - promotionPrice)));
        // 修改 SKU 的优惠
        modifyOrderItemPayPrice(orderItem, promotionPrice, priceCalculate);
    }

    private void addPromotion(PriceCalculateRespDTO priceCalculate, PriceCalculateRespDTO.OrderItem orderItem,
                              Long id, String name, Integer type, Integer level,
                              Integer newPayPrice, Boolean meet, String meetTip) {
        // 创建营销明细 Item
        PriceCalculateRespDTO.PromotionItem promotionItem = new PriceCalculateRespDTO.PromotionItem().setSkuId(orderItem.getSkuId())
                .setOriginalPrice(orderItem.getPayPrice()).setDiscountPrice(orderItem.getPayPrice() - newPayPrice);
        // 创建营销明细
        PriceCalculateRespDTO.Promotion promotion = new PriceCalculateRespDTO.Promotion()
                .setId(id).setName(name).setType(type).setLevel(level)
                .setOriginalPrice(promotionItem.getOriginalPrice()).setDiscountPrice(promotionItem.getDiscountPrice())
                .setItems(singletonList(promotionItem)).setMeet(meet).setMeetTip(meetTip);
        priceCalculate.getPromotions().add(promotion);
    }

    private void modifyOrderItemPayPrice(PriceCalculateRespDTO.OrderItem orderItem, Integer newPayPrice,
                                         PriceCalculateRespDTO priceCalculate) {
        int diffPayPrice = orderItem.getPayPrice() - newPayPrice;
        // 设置 OrderItem 价格相关字段
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + diffPayPrice);
        orderItem.setPayPrice(newPayPrice);
        orderItem.setOrderDividePrice(orderItem.getPayPrice() - orderItem.getOrderPartPrice());
        // 设置 Order 相关相关字段
        priceCalculate.getOrder().setPayPrice(priceCalculate.getOrder().getPayPrice() - diffPayPrice);
    }

    // TODO 芋艿：提前实现
    private Supplier<Double> getMemberDiscountSupplier(Long userId) {
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

    private String formatPrice(Integer price) {
        return String.format("%.2f", price / 100d);
    }

}

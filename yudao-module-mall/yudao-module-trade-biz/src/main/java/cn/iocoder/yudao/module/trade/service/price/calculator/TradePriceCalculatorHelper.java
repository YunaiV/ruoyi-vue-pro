package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static java.util.Collections.singletonList;

/**
 * {@link TradePriceCalculator} 的工具类
 *
 * 主要实现对 {@link TradePriceCalculateRespBO} 计算结果的操作
 *
 * @author 芋道源码
 */
public class TradePriceCalculatorHelper {

    public static TradePriceCalculateRespBO buildCalculateResp(TradePriceCalculateReqBO param,
                                                               List<ProductSpuRespDTO> spuList, List<ProductSkuRespDTO> skuList) {
        // 创建 PriceCalculateRespDTO 对象
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO();
        result.setType(getOrderType(param)).setPromotions(new ArrayList<>()).setGiveCouponsMap(new LinkedHashMap<>());

        // 创建它的 OrderItem 属性
        result.setItems(new ArrayList<>(param.getItems().size()));
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skuList, ProductSkuRespDTO::getId);
        param.getItems().forEach(item -> {
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                return;
            }
            ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());
            if (spu == null) {
                return;
            }
            // 商品项
            TradePriceCalculateRespBO.OrderItem orderItem = new TradePriceCalculateRespBO.OrderItem();
            result.getItems().add(orderItem);
            orderItem.setSpuId(sku.getSpuId()).setSkuId(sku.getId())
                    .setCount(item.getCount()).setCartId(item.getCartId()).setSelected(item.getSelected());
            // sku 价格
            orderItem.setPrice(sku.getPrice()).setPayPrice(sku.getPrice() * item.getCount())
                    .setDiscountPrice(0).setDeliveryPrice(0).setCouponPrice(0).setPointPrice(0).setVipPrice(0);
            // sku 信息
            orderItem.setPicUrl(sku.getPicUrl()).setProperties(sku.getProperties())
                    .setWeight(sku.getWeight()).setVolume(sku.getVolume());
            // spu 信息
            orderItem.setSpuName(spu.getName()).setCategoryId(spu.getCategoryId())
                    .setDeliveryTypes(spu.getDeliveryTypes()).setDeliveryTemplateId(spu.getDeliveryTemplateId())
                    .setGivePoint(spu.getGiveIntegral()).setUsePoint(0);
            if (StrUtil.isBlank(orderItem.getPicUrl())) {
                orderItem.setPicUrl(spu.getPicUrl());
            }
        });

        // 创建它的 Price 属性
        result.setPrice(new TradePriceCalculateRespBO.Price());
        recountAllPrice(result);
        recountAllGivePoint(result);
        return result;
    }

    /**
     * 计算订单类型
     *
     * @param param 计算参数
     * @return 订单类型
     */
    private static Integer getOrderType(TradePriceCalculateReqBO param) {
        if (param.getSeckillActivityId() != null) {
            return TradeOrderTypeEnum.SECKILL.getType();
        }
        if (param.getCombinationActivityId() != null) {
            return TradeOrderTypeEnum.COMBINATION.getType();
        }
        if (param.getBargainRecordId() != null) {
            return TradeOrderTypeEnum.BARGAIN.getType();
        }
        return TradeOrderTypeEnum.NORMAL.getType();
    }

    /**
     * 基于订单项，重新计算 price 总价
     *
     * @param result 计算结果
     */
    public static void recountAllPrice(TradePriceCalculateRespBO result) {
        // 先重置
        TradePriceCalculateRespBO.Price price = result.getPrice();
        price.setTotalPrice(0).setDiscountPrice(0).setDeliveryPrice(0)
                .setCouponPrice(0).setPointPrice(0).setVipPrice(0).setPayPrice(0);
        // 再合计 item
        result.getItems().forEach(item -> {
            if (!item.getSelected()) {
                return;
            }
            price.setTotalPrice(price.getTotalPrice() + item.getPrice() * item.getCount());
            price.setDiscountPrice(price.getDiscountPrice() + item.getDiscountPrice());
            price.setDeliveryPrice(price.getDeliveryPrice() + item.getDeliveryPrice());
            price.setCouponPrice(price.getCouponPrice() + item.getCouponPrice());
            price.setPointPrice(price.getPointPrice() + item.getPointPrice());
            price.setVipPrice(price.getVipPrice() + item.getVipPrice());
            price.setPayPrice(price.getPayPrice() + item.getPayPrice());
        });
    }

    /**
     * 基于订单项，重新计算赠送积分
     *
     * @param result 计算结果
     */
    public static void recountAllGivePoint(TradePriceCalculateRespBO result) {
        result.setGivePoint(getSumValue(result.getItems(), item -> item.getSelected() ? item.getGivePoint() : 0, Integer::sum));
    }

    /**
     * 重新计算单个订单项的支付金额
     *
     * @param orderItem 订单项
     */
    public static void recountPayPrice(TradePriceCalculateRespBO.OrderItem orderItem) {
        orderItem.setPayPrice(orderItem.getPrice() * orderItem.getCount()
                - orderItem.getDiscountPrice()
                + orderItem.getDeliveryPrice()
                - orderItem.getCouponPrice()
                - orderItem.getPointPrice()
                - orderItem.getVipPrice()
        );
    }

    /**
     * 重新计算每个订单项的支付金额
     *
     * 【目前主要是单测使用】
     *
     * @param orderItems 订单项数组
     */
    public static void recountPayPrice(List<TradePriceCalculateRespBO.OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            if (orderItem.getDiscountPrice() == null) {
                orderItem.setDiscountPrice(0);
            }
            if (orderItem.getDeliveryPrice() == null) {
                orderItem.setDeliveryPrice(0);
            }
            if (orderItem.getCouponPrice() == null) {
                orderItem.setCouponPrice(0);
            }
            if (orderItem.getPointPrice() == null) {
                orderItem.setPointPrice(0);
            }
            if (orderItem.getUsePoint() == null) {
                orderItem.setUsePoint(0);
            }
            if (orderItem.getGivePoint() == null) {
                orderItem.setGivePoint(0);
            }
            if (orderItem.getVipPrice() == null) {
                orderItem.setVipPrice(0);
            }
            recountPayPrice(orderItem);
        });
    }

    /**
     * 计算已选中的订单项，总支付金额
     *
     * @param orderItems 订单项数组
     * @return 总支付金额
     */
    public static Integer calculateTotalPayPrice(List<TradePriceCalculateRespBO.OrderItem> orderItems) {
        return getSumValue(orderItems,
                orderItem -> orderItem.getSelected() ? orderItem.getPayPrice() : 0, // 未选中的情况下，不计算支付金额
                Integer::sum);
    }

    /**
     * 计算已选中的订单项，总商品数
     *
     * @param orderItems 订单项数组
     * @return 总商品数
     */
    public static Integer calculateTotalCount(List<TradePriceCalculateRespBO.OrderItem> orderItems) {
        return getSumValue(orderItems,
                orderItem -> orderItem.getSelected() ? orderItem.getCount() : 0, // 未选中的情况下，不计算数量
                Integer::sum);
    }

    /**
     * 按照支付金额，返回每个订单项的分摊金额数组
     *
     * 实际上 price 不仅仅可以传递的是金额，也可以是积分。因为它的实现逻辑，就是根据 payPrice 做分摊而已
     *
     * @param orderItems 订单项数组
     * @param price      金额
     * @return 分摊金额数组，和传入的 orderItems 一一对应
     */
    public static List<Integer> dividePrice(List<TradePriceCalculateRespBO.OrderItem> orderItems, Integer price) {
        Integer total = calculateTotalPayPrice(orderItems);
        assert total != null;
        // 遍历每一个，进行分摊
        List<Integer> prices = new ArrayList<>(orderItems.size());
        int remainPrice = price;
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            // 1. 如果是未选中，则分摊为 0
            if (!orderItem.getSelected()) {
                prices.add(0);
                continue;
            }
            // 2. 如果选中，则按照百分比，进行分摊
            int partPrice;
            if (i < orderItems.size() - 1) { // 减一的原因，是因为拆分时，如果按照比例，可能会出现.所以最后一个，使用反减
                partPrice = (int) (price * (1.0D * orderItem.getPayPrice() / total));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            Assert.isTrue(partPrice >= 0, "分摊金额必须大于等于 0");
            prices.add(partPrice);
        }
        return prices;
    }

    /**
     * 计算订单调价价格分摊
     *
     * 和 {@link #dividePrice(List, Integer)} 逻辑一致，只是传入的是 TradeOrderItemDO 对象
     *
     * @param items 订单项
     * @param price 订单支付金额
     * @return 分摊金额数组，和传入的 orderItems 一一对应
     */
    public static List<Integer> dividePrice2(List<TradeOrderItemDO> items, Integer price) {
        Integer total = getSumValue(items, TradeOrderItemDO::getPayPrice, Integer::sum);
        assert total != null;
        // 遍历每一个，进行分摊
        List<Integer> prices = new ArrayList<>(items.size());
        int remainPrice = price;
        for (int i = 0; i < items.size(); i++) {
            TradeOrderItemDO orderItem = items.get(i);
            int partPrice;
            if (i < items.size() - 1) { // 减一的原因，是因为拆分时，如果按照比例，可能会出现.所以最后一个，使用反减
                partPrice = (int) (price * (1.0D * orderItem.getPrice() / total));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            prices.add(partPrice);
        }
        return prices;
    }

    /**
     * 添加【匹配】单个 OrderItem 的营销明细
     *
     * @param result        价格计算结果
     * @param orderItem     单个订单商品 SKU
     * @param id            营销编号
     * @param name          营销名字
     * @param description   满足条件的提示
     * @param type          营销类型
     * @param discountPrice 单个订单商品 SKU 的优惠价格（总）
     */
    public static void addPromotion(TradePriceCalculateRespBO result, TradePriceCalculateRespBO.OrderItem orderItem,
                                    Long id, String name, Integer type, String description, Integer discountPrice) {
        addPromotion(result, singletonList(orderItem), id, name, type, description, singletonList(discountPrice));
    }

    /**
     * 添加【匹配】多个 OrderItem 的营销明细
     *
     * @param result         价格计算结果
     * @param orderItems     多个订单商品 SKU
     * @param id             营销编号
     * @param name           营销名字
     * @param description    满足条件的提示
     * @param type           营销类型
     * @param discountPrices 多个订单商品 SKU 的优惠价格（总），和 orderItems 一一对应
     */
    public static void addPromotion(TradePriceCalculateRespBO result, List<TradePriceCalculateRespBO.OrderItem> orderItems,
                                    Long id, String name, Integer type, String description, List<Integer> discountPrices) {
        // 创建营销明细 Item
        List<TradePriceCalculateRespBO.PromotionItem> promotionItems = new ArrayList<>(discountPrices.size());
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            promotionItems.add(new TradePriceCalculateRespBO.PromotionItem().setSkuId(orderItem.getSkuId())
                    .setTotalPrice(orderItem.getPayPrice()).setDiscountPrice(discountPrices.get(i)));
        }
        // 创建营销明细
        TradePriceCalculateRespBO.Promotion promotion = new TradePriceCalculateRespBO.Promotion()
                .setId(id).setName(name).setType(type)
                .setTotalPrice(calculateTotalPayPrice(orderItems))
                .setDiscountPrice(getSumValue(discountPrices, value -> value, Integer::sum))
                .setItems(promotionItems).setMatch(true).setDescription(description);
        result.getPromotions().add(promotion);
    }

    /**
     * 添加【不匹配】多个 OrderItem 的营销明细
     *
     * @param result      价格计算结果
     * @param orderItems  多个订单商品 SKU
     * @param id          营销编号
     * @param name        营销名字
     * @param description 满足条件的提示
     * @param type        营销类型
     */
    public static void addNotMatchPromotion(TradePriceCalculateRespBO result, List<TradePriceCalculateRespBO.OrderItem> orderItems,
                                            Long id, String name, Integer type, String description) {
        // 创建营销明细 Item
        List<TradePriceCalculateRespBO.PromotionItem> promotionItems = CollectionUtils.convertList(orderItems,
                orderItem -> new TradePriceCalculateRespBO.PromotionItem().setSkuId(orderItem.getSkuId())
                        .setTotalPrice(orderItem.getPayPrice()).setDiscountPrice(0));
        // 创建营销明细
        TradePriceCalculateRespBO.Promotion promotion = new TradePriceCalculateRespBO.Promotion()
                .setId(id).setName(name).setType(type)
                .setTotalPrice(calculateTotalPayPrice(orderItems))
                .setDiscountPrice(0)
                .setItems(promotionItems).setMatch(false).setDescription(description);
        result.getPromotions().add(promotion);
    }

    public static String formatPrice(Integer price) {
        return String.format("%.2f", price / 100d);
    }

}

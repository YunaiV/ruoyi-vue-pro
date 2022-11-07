package cn.iocoder.yudao.module.promotion.convert.price;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface PriceConvert {

    PriceConvert INSTANCE = Mappers.getMapper(PriceConvert.class);

    default PriceCalculateRespDTO convert(PriceCalculateReqDTO calculateReqDTO, List<ProductSkuRespDTO> skuList) {
        // 创建 PriceCalculateRespDTO 对象
        PriceCalculateRespDTO priceCalculate = new PriceCalculateRespDTO();
        // 创建它的 Order 属性
        PriceCalculateRespDTO.Order order = new PriceCalculateRespDTO.Order().setOriginalPrice(0).setDiscountPrice(0)
                .setCouponPrice(0).setPointPrice(0).setDeliveryPrice(0).setPayPrice(0)
                .setItems(new ArrayList<>()).setCouponId(calculateReqDTO.getCouponId());
        priceCalculate.setOrder(order).setPromotions(new ArrayList<>());
        // 创建它的 OrderItem 属性
        Map<Long, Integer> skuIdCountMap = CollectionUtils.convertMap(calculateReqDTO.getItems(),
                PriceCalculateReqDTO.Item::getSkuId, PriceCalculateReqDTO.Item::getCount);
        skuList.forEach(sku -> {
            Integer count = skuIdCountMap.get(sku.getId());
            PriceCalculateRespDTO.OrderItem orderItem = new PriceCalculateRespDTO.OrderItem()
                    .setSpuId(sku.getSpuId()).setSkuId(sku.getId()).setCount(count)
                    .setOriginalUnitPrice(sku.getPrice()).setOriginalPrice(sku.getPrice() * count)
                    .setDiscountPrice(0).setOrderPartPrice(0);
            orderItem.setPayPrice(orderItem.getOriginalPrice()).setOrderDividePrice(orderItem.getOriginalPrice());
            priceCalculate.getOrder().getItems().add(orderItem);
            // 补充价格信息到 Order 中
            order.setOriginalPrice(order.getOriginalPrice() + orderItem.getOriginalPrice())
                    .setOrderPrice(order.getOriginalPrice()).setPayPrice(order.getOriginalPrice());
        });
        return priceCalculate;
    }

    CouponMeetRespDTO convert(CouponDO coupon);

}

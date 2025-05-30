package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.walmart.model.WalmartToken;
import com.somle.walmart.model.reps.WalmartAllProductsRepsVO;
import com.somle.walmart.model.reps.WalmartOrderResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class WalmartToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;


    private PlatformEnum platform = PlatformEnum.WALMART;


    public OmsShopSaveReqDTO toShops(WalmartToken token) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, token.getClientId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(token.getClientId());
        shopDTO.setCode(null);
        shopDTO.setExternalId(token.getClientId());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<WalmartAllProductsRepsVO.ItemResponseDTO> products, WalmartToken walmartToken) {
        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(walmartToken.getClientId());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));

        HashSet<String> unique = new HashSet<>();
        List<OmsShopProductSaveReqDTO> omsShopProductDOs = new ArrayList<>();
        for (WalmartAllProductsRepsVO.ItemResponseDTO walmartProductRepsDTO : products) {
            OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
            MapUtils.findAndThen(existShopProductMap, walmartProductRepsDTO.getSku() + "#" + walmartProductRepsDTO.getUpc(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
            shopProductDTO.setShopId(omsShopDTO.getId());
            shopProductDTO.setCode(walmartProductRepsDTO.getSku());
            shopProductDTO.setExternalId(walmartProductRepsDTO.getSku() + "#" + walmartProductRepsDTO.getUpc());
            if (unique.contains(shopProductDTO.getExternalId())) {
                continue;
            }
            unique.add(shopProductDTO.getExternalId());
            shopProductDTO.setName(walmartProductRepsDTO.getProductName());
            shopProductDTO.setSellableQty(walmartProductRepsDTO.getSellableQty());
            if (walmartProductRepsDTO.getPrice() != null) {
                shopProductDTO.setPrice(new BigDecimal(walmartProductRepsDTO.getPrice().getAmount()));
            }
            omsShopProductDOs.add(shopProductDTO);
        }
        return omsShopProductDOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(WalmartOrderResponse walmartOrderResponse, WalmartToken walmartToken) {
        List<WalmartOrderResponse.Order> orders = walmartOrderResponse.getList().getElements().getOrder();
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }


        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        // key是platformShopCode
        Map<String, OmsShopDTO> omsShopMap = omsShopApi.getByPlatformCode(this.platform.toString())
            .stream().collect(Collectors.toMap(OmsShopDTO::getExternalId, Function.identity()));

        List<Long> shopIds = omsShopMap.values().stream().map(OmsShopDTO::getId).toList();
        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(shopIds);
        // 使用Map存储已存在的店铺产品信息，key=shopId, value=Map<String, OmsShopProductDTO>,其中key=code即平台sku
        Map<Long, Map<String, OmsShopProductDTO>> existShopProductMap = existShopProducts.stream()
            .collect(Collectors.groupingBy(
                OmsShopProductDTO::getShopId,  // 外层Map的key: shopId
                Collectors.toMap(
                    OmsShopProductDTO::getCode,  // 内层Map的key: code
                    product -> product,          // 内层Map的value: 产品对象本身
                    (existing, replacement) -> existing  // 如果code重复，保留已存在的
                )
            ));


        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();
        for (WalmartOrderResponse.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getPurchaseOrderId() + "#" + order.getCustomerOrderId(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            MapUtils.findAndThen(omsShopMap, walmartToken.getClientId(), omsShopDTO -> omsOrderSaveReqDTO.setShopId(omsShopDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getPurchaseOrderId() + "#" + order.getCustomerOrderId());
            omsOrderSaveReqDTO.setEmail(order.getCustomerEmailId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getOrderDate());
            omsOrderSaveReqDTO.setPayTime(order.getOrderDate());

            WalmartOrderResponse.Order.ShippingInfo shippingInfo = order.getShippingInfo();
            if (shippingInfo != null) {
                omsOrderSaveReqDTO.setPhone(shippingInfo.getPhone());
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingInfo.getPostalAddress()));
                omsOrderSaveReqDTO.setOutboundLatestTime(shippingInfo.getEstimatedShipDate());
            }


            WalmartOrderResponse.Order.ShippingInfo.PostalAddress postalAddress = shippingInfo.getPostalAddress();
            if (postalAddress != null) {
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(postalAddress));
                omsOrderSaveReqDTO.setAddress1(postalAddress.getAddress1());
                omsOrderSaveReqDTO.setAddress2(postalAddress.getAddress2());
                omsOrderSaveReqDTO.setCity(postalAddress.getCity());
                omsOrderSaveReqDTO.setCity(postalAddress.getCity());
                omsOrderSaveReqDTO.setState(postalAddress.getState());
                omsOrderSaveReqDTO.setPostalCode(postalAddress.getPostalCode());
                omsOrderSaveReqDTO.setRecipientName(postalAddress.getName());
                omsOrderSaveReqDTO.setRecipientCountryCode(postalAddress.getCountry());
                omsOrderSaveReqDTO.setBuyerName(shippingInfo.getPostalAddress().getName());
            }

            WalmartOrderResponse.Order.OrderLines orderLineList = order.getOrderLines();
            List<WalmartOrderResponse.Order.OrderLines.OrderLine> orderLines = orderLineList.getOrderLine();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            //总运费
            BigDecimal shippingFee = new BigDecimal(0);
            BigDecimal totalPrice = new BigDecimal(0);
            for (WalmartOrderResponse.Order.OrderLines.OrderLine orderLine : orderLines) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setShopProductId(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()).get(orderLine.getItem().getSku()).getId());
                for (WalmartOrderResponse.Order.OrderLines.OrderLine.Charges.Charge charge : orderLine.getCharges().getCharge()) {
                    if ("SHIPPING".equals(charge.getChargeType())) {
                        shippingFee = shippingFee.add(charge.getChargeAmount().getAmount());
                        if (charge.getTax() != null && charge.getTax().getTaxAmount() != null) {
                            shippingFee = shippingFee.add(charge.getTax().getTaxAmount().getAmount());
                        }

                    }

                    if ("PRODUCT".equals(charge.getChargeType())) {
                        BigDecimal itemPrice = charge.getChargeAmount().getAmount();
                        if (charge.getTax() != null && charge.getTax().getTaxAmount() != null) {
                            itemPrice = itemPrice.add(charge.getTax().getTaxAmount().getAmount());
                        }
                        totalPrice = totalPrice.add(itemPrice);
                        omsOrderItemSaveReqDTO.setPrice(itemPrice);
                    }
                }
                omsOrderItemSaveReqDTO.setQty(orderLine.getOrderLineQuantity().getAmount());
                omsOrderItemSaveReqDTO.setExternalId(orderLine.getLineNumber());
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setShippingFee(shippingFee);
            omsOrderSaveReqDTO.setTotalPrice(totalPrice);
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

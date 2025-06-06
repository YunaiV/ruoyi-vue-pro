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
import com.somle.otto.model.pojo.OttoAccount;
import com.somle.otto.model.resp.OttoOrder;
import com.somle.otto.model.resp.OttoProductResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class OttoToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.OTTO;

    public OmsShopSaveReqDTO toShops(OttoAccount ottoAccount) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, ottoAccount.getClientId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(ottoAccount.getClientId());
        shopDTO.setCode(null);
        shopDTO.setExternalId(ottoAccount.getClientId());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<OttoProductResp.ProductVariation> products, OttoAccount ottoAccount) {

        if (CollectionUtil.isEmpty(products)) {
            return Collections.emptyList();
        }

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(ottoAccount.getClientId());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }
        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));

        List<OmsShopProductSaveReqDTO> omsShopProductDOs = new ArrayList<>();
        for (OttoProductResp.ProductVariation product : products) {
            OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
            MapUtils.findAndThen(existShopProductMap, product.getSku() + "#" + omsShopDTO.getId(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
            shopProductDTO.setShopId(omsShopDTO.getId());
            shopProductDTO.setCode(product.getSku());
            shopProductDTO.setExternalId(product.getSku() + "#" + omsShopDTO.getId());
            shopProductDTO.setName(product.getProductReference());
            shopProductDTO.setSellableQty(product.getQuantity());
            if (product.getPricing() != null && product.getPricing().getSale() != null) {
                shopProductDTO.setPrice(new BigDecimal(product.getPricing().getSale().getSalePrice().getAmount()));
            }
            omsShopProductDOs.add(shopProductDTO);
        }
        return omsShopProductDOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<OttoOrder> orders, OttoAccount ottoAccount) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(ottoAccount.getClientId());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = externalId, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (OttoOrder order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderNumber(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderNumber());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getOrderDate().plusHours(8));
            omsOrderSaveReqDTO.setPayTime(order.getOrderDate().plusHours(8));
            BigDecimal totalPrice = BigDecimal.ZERO;
            OttoOrder.Address deliveryAddress = order.getDeliveryAddress();
            if (deliveryAddress != null) {
                omsOrderSaveReqDTO.setBuyerName(deliveryAddress.getFirstName() + " " + deliveryAddress.getLastName());
                omsOrderSaveReqDTO.setEmail(deliveryAddress.getEmail());
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(deliveryAddress));
                omsOrderSaveReqDTO.setRecipientName(deliveryAddress.getFirstName() + " " + deliveryAddress.getLastName());
                omsOrderSaveReqDTO.setCity(deliveryAddress.getCity());
                omsOrderSaveReqDTO.setPostalCode(deliveryAddress.getZipCode());
                omsOrderSaveReqDTO.setBuyerCountryCode(deliveryAddress.getCountryCode());
                omsOrderSaveReqDTO.setAddress1(deliveryAddress.getStreet());
                omsOrderSaveReqDTO.setHouseNo(deliveryAddress.getHouseNumber());
            }

            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            HashMap<String, Integer> skuMap = new HashMap<>();

            for (OttoOrder.PositionItem positionItem : order.getPositionItems()) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                String sku = positionItem.getProduct().getSku();
                skuMap.put(sku, skuMap.getOrDefault(sku, 0) + 1);
                omsOrderItemSaveReqDTO.setExternalId(order.getOrderNumber() + "#" + sku);
                omsOrderItemSaveReqDTO.setShopProductExternalCode(sku);
                omsOrderItemSaveReqDTO.setQty(skuMap.getOrDefault(sku, 0));
                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(positionItem.getItemValueGrossPrice().getAmount()));
                totalPrice = totalPrice.add(new BigDecimal(positionItem.getItemValueGrossPrice().getAmount()));
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }

            omsOrderSaveReqDTO.setTotalPrice(totalPrice);
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

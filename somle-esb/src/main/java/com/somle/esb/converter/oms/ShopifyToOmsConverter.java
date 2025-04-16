package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.shopify.model.reps.ShopifyOrderRepsVO;
import com.somle.shopify.model.reps.ShopifyShopProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import com.somle.shopify.service.ShopifyClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;


@Component
public class ShopifyToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    private PlatformEnum platform = PlatformEnum.SHOPIFY;

    public List<OmsShopSaveReqDTO> toShops(List<ShopifyShopRepsVO> shopInfoDTOs) {
        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            shopDTO.setName(null);
            shopDTO.setPlatformShopName(shopInfoDTO.getName());
            shopDTO.setCode(null);
            shopDTO.setPlatformShopCode(shopInfoDTO.getId().toString());
            shopDTO.setPlatformCode(this.platform.toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<ShopifyShopProductRepsVO> product, ShopifyClient shopifyClient) {
        if (CollectionUtil.isEmpty(shopifyClient.getShops())) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        ShopifyShopRepsVO shopifyShopRepsVO = shopifyClient.getShops().get(0);


        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());

        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getVariants().stream()
                    .map(variant -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        shopProductDTO.setShopId(omsShopDO.getId());
                        shopProductDTO.setCode(variant.getSku());
                        shopProductDTO.setSourceId(variant.getId().toString());
                        shopProductDTO.setName(variant.getTitle());
                        shopProductDTO.setPrice(new BigDecimal(variant.getPrice()));
                        shopProductDTO.setSellableQty(variant.getInventoryQuantity());
                        return shopProductDTO;
                    })
            ).toList();
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<ShopifyOrderRepsVO> orders, ShopifyClient shopifyClient) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        ShopifyShopRepsVO shopifyShopRepsVO = shopifyClient.getShops().get(0);
        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());
        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = orders.stream()
            .map(order -> {
                OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
                omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
                omsOrderSaveReqDTO.setSourceNo(order.getId().toString());
                omsOrderSaveReqDTO.setShopId(omsShopDO.getId());
                omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getTotalPrice()));
                omsOrderSaveReqDTO.setBuyerName(order.getCustomer().getFirstName() + order.getCustomer().getLastName());
                omsOrderSaveReqDTO.setEmail(order.getCustomer().getEmail());
                omsOrderSaveReqDTO.setOrderCreateTime(ZonedDateTime.parse(order.getCreatedAt()).toLocalDateTime());
                omsOrderSaveReqDTO.setTelephone(order.getCustomer().getPhone());
                omsOrderSaveReqDTO.setBuyerCountryCode(order.getCustomer().getDefaultAddress().getCountryCode());
                omsOrderSaveReqDTO.setState(order.getCustomer().getState());
                omsOrderSaveReqDTO.setCity(order.getCustomer().getDefaultAddress().getCity());
                omsOrderSaveReqDTO.setSourceAddress(JsonUtilsX.toJsonString(order.getShippingAddress()));

                //转换地址
                ShopifyOrderRepsVO.ShippingAddressDTO shippingAddress = order.getShippingAddress();
                String address = shippingAddress.getCountry() + " " + shippingAddress.getProvince() + " " + shippingAddress.getCity() + " " + shippingAddress.getAddress1();
                omsOrderSaveReqDTO.setAddress(address);

                List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = order.getLineItems().stream()
                    .map(lineItem -> {
                        OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                        omsOrderItemSaveReqDTO.setShopProductCode(lineItem.getSku());
                        omsOrderItemSaveReqDTO.setQty(lineItem.getQuantity());
                        omsOrderItemSaveReqDTO.setPrice(new BigDecimal(lineItem.getPrice()));
                        return omsOrderItemSaveReqDTO;
                    }).toList();
                omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
                return omsOrderSaveReqDTO;
            }).toList();
        return omsOrderSaveReqDTOs;
    }
}

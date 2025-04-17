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
import com.somle.shopify.model.reps.ShopifyOrderRepsVO;
import com.somle.shopify.model.reps.ShopifyShopProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import com.somle.shopify.service.ShopifyClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;


@Component
@Slf4j
public class ShopifyToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;
    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;

    private ShopifyShopRepsVO shopifyShopRepsVO;

    private PlatformEnum platform = PlatformEnum.SHOPIFY;

    public OmsShopSaveReqDTO toShops(ShopifyShopRepsVO shopInfoDTO) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getPlatformShopCode(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, shopInfoDTO.getId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setPlatformShopName(shopInfoDTO.getName());
        shopDTO.setCode(null);
        shopDTO.setPlatformShopCode(shopInfoDTO.getId().toString());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<ShopifyShopProductRepsVO> product, ShopifyClient shopifyClient) {
        if (shopifyClient.getShop() == null) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        if (shopifyShopRepsVO == null) {
            shopifyShopRepsVO = shopifyClient.getShop();
        }


        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());

        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getSourceId(), omsShopProductDO -> omsShopProductDO));


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getVariants().stream()
                    .map(variant -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        MapUtils.findAndThen(existShopProductMap, variant.getId().toString(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                        shopProductDTO.setShopId(omsShopDO.getId());
                        shopProductDTO.setCode(variant.getSku());
                        shopProductDTO.setSourceId(variant.getId().toString());
                        shopProductDTO.setName(variant.getTitle());
                        if (variant.getPrice() != null) {
                            shopProductDTO.setPrice(new BigDecimal(variant.getPrice()));
                        }
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
        if (shopifyShopRepsVO == null) {
            shopifyShopRepsVO = shopifyClient.getShop();
        }

        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());
        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getSourceNo(), omsOrderDTO -> omsOrderDTO));


        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = orders.stream()
            .map(order -> {
                try {
                    OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
                    MapUtils.findAndThen(existOrderMap, order.getId().toString(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
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
                    omsOrderSaveReqDTO.setAddress(order.getCustomer().getDefaultAddress().getAddress1());
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
                } catch (Exception e) {
                    log.info("转换Shopify订单异常", e);
                }
                return null;
            }).toList();
        return omsOrderSaveReqDTOs;
    }
}

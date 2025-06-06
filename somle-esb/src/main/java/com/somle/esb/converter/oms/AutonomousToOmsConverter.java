package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.autonomous.model.AutonomousAccount;
import com.somle.autonomous.resp.AutonomousOrderResp;
import com.somle.autonomous.resp.AutonomousProductResp;
import com.somle.esb.enums.PlatformEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class AutonomousToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.AUTONOMOUS;

    public OmsShopSaveReqDTO toShops(AutonomousAccount autonomousAccount) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, autonomousAccount.getEmail(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(autonomousAccount.getEmail());
        shopDTO.setCode(null);
        shopDTO.setExternalId(autonomousAccount.getEmail());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<AutonomousProductResp.Product> products, AutonomousAccount autonomousAccount) {

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(autonomousAccount.getEmail());
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
        for (AutonomousProductResp.Product product : products) {
            List<String> skus = product.getAliasSkus();
            for (int i = 0; i < skus.size(); i++) {
                OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                String sku = skus.get(i);
                AutonomousProductResp.ProductUrl productUrl = product.getProductUrls().get(i);
                MapUtils.findAndThen(existShopProductMap, sku + "#" + productUrl.getSequenceCode(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                shopProductDTO.setShopId(omsShopDTO.getId());
                shopProductDTO.setCode(sku);
                shopProductDTO.setExternalId(sku + "#" + productUrl.getSequenceCode());
                shopProductDTO.setName(productUrl.getName());
                shopProductDTO.setUrl(productUrl.getSeoUrl());
//                if (ObjectUtil.isNotEmpty(productUrl.getPrice()) && ObjectUtil.isNotEmpty(productUrl.getPrice().getUsd())) {
//                    shopProductDTO.setPrice(new BigDecimal(productUrl.getPrice().getUsd()));
//                }
                omsShopProductDOs.add(shopProductDTO);
            }
        }
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<AutonomousOrderResp.OrderDetail> orders, AutonomousAccount autonomousAccount) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(autonomousAccount.getEmail());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (AutonomousOrderResp.OrderDetail order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderDetailId(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderDetailCode());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getDateCreated());
            if (order.getAmount() != null) {
                omsOrderSaveReqDTO.setTotalPrice(order.getAmount());
            }

            omsOrderSaveReqDTO.setBuyerName(order.getFullName());
            omsOrderSaveReqDTO.setEmail(order.getEmail());
            omsOrderSaveReqDTO.setExternalAddress(order.getShippingAddress());
            omsOrderSaveReqDTO.setRecipientName(order.getFullName());
            omsOrderSaveReqDTO.setAddress1(order.getShippingName());
            omsOrderSaveReqDTO.setRecipientCountryCode(order.getCountry());
            omsOrderSaveReqDTO.setState(order.getStateRegion());
            omsOrderSaveReqDTO.setCity(order.getCity());
            omsOrderSaveReqDTO.setPostalCode(order.getPostalCode());


            Collection<String> skus = order.getSkus().values();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (String sku : skus) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setShopProductExternalCode(sku);
                omsOrderItemSaveReqDTO.setExternalId(order.getOrderDetailId());
                omsOrderItemSaveReqDTO.setQty(order.getQuantity());
                omsOrderItemSaveReqDTO.setPrice(order.getProductPrice());
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

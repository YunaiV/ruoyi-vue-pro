package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.wayfair.model.WayfairToken;
import com.somle.wayfair.model.reps.WayfairOrderRepsVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WayfairToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;
    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.WAYFAIR;


    public OmsShopSaveReqDTO toShops(WayfairToken wayfairToken) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, wayfairToken.getClientId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(wayfairToken.getClientId());
        shopDTO.setCode(null);
        shopDTO.setExternalId(wayfairToken.getClientId());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsOrderSaveReqDTO> toOrders(WayfairOrderRepsVO wayfairOrderRepsVO, WayfairToken wayfairToken) {
        List<WayfairOrderRepsVO.DropshipPurchaseOrder> orders = wayfairOrderRepsVO.getData().getGetDropshipPurchaseOrders();
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }


        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalCode(), omsOrderDTO -> omsOrderDTO));

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));


        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();
        for (WayfairOrderRepsVO.DropshipPurchaseOrder order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderId().toString() + order.getPoNumber(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            MapUtils.findAndThen(existShopMap, wayfairToken.getClientId(), omsShopDTO -> omsOrderSaveReqDTO.setShopId(omsShopDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalCode(order.getOrderId().toString() + order.getPoNumber());
            omsOrderSaveReqDTO.setBuyerName(order.getCustomerName());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getPoDate());

            WayfairOrderRepsVO.DropshipPurchaseOrder.ShipTo shipTo = order.getShipTo();
            if (shipTo != null) {
                omsOrderSaveReqDTO.setPhone(shipTo.getPhoneNumber());
                omsOrderSaveReqDTO.setAddress1(shipTo.getAddress1());
                omsOrderSaveReqDTO.setAddress2(shipTo.getAddress2());
                omsOrderSaveReqDTO.setAddress3(shipTo.getAddress3());
                omsOrderSaveReqDTO.setCity(shipTo.getCity());
                omsOrderSaveReqDTO.setState(shipTo.getState());
                omsOrderSaveReqDTO.setPostalCode(shipTo.getPostalCode());
                omsOrderSaveReqDTO.setRecipientName(shipTo.getName());
                omsOrderSaveReqDTO.setRecipientCountryCode(shipTo.getCountry());
            }

            List<WayfairOrderRepsVO.DropshipPurchaseOrder.Product> orderItems = order.getProducts();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (WayfairOrderRepsVO.DropshipPurchaseOrder.Product orderItem : orderItems) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setQty(orderItem.getQuantity());
                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(orderItem.getPrice()));
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);

            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

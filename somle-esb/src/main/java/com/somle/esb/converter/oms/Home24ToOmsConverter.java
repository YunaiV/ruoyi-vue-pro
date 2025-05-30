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
import com.somle.home24.model.resp.Home24OrderResp;
import com.somle.home24.model.resp.Home24ShopResp;
import com.somle.home24.service.Home24Client;
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
public class Home24ToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.HOME24;

    private Home24ShopResp home24ShopResp;

    public OmsShopSaveReqDTO toShops(Home24ShopResp home24ShopResp) {
        this.home24ShopResp = home24ShopResp;
        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));
        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, home24ShopResp.getShopId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(home24ShopResp.getShopName());
        shopDTO.setCode(null);
        shopDTO.setExternalId(home24ShopResp.getShopId().toString());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<Home24OrderResp.Order> orders, Home24Client home24Client) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }


        if (home24ShopResp == null) {
            home24ShopResp = home24Client.getShopInformation();
        }

        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(home24ShopResp.getShopId().toString());
        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));


        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (Home24OrderResp.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getCommercialId(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getCommercialId());
            omsOrderSaveReqDTO.setShopId(omsShopDO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getCreatedDate().plusHours(2));
            omsOrderSaveReqDTO.setPayTime(order.getCustomerDebitedDate().plusHours(2));
            omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getPrice()));
            Home24OrderResp.Customer customer = order.getCustomer();

            if (customer != null) {
                omsOrderSaveReqDTO.setBuyerName(customer.getFirstname() + customer.getLastname());
                Home24OrderResp.ShippingAddress shippingAddress = customer.getShippingAddress();
                if (shippingAddress != null) {
                    omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingAddress));
                    omsOrderSaveReqDTO.setRecipientName(shippingAddress.getFirstname() + " " + shippingAddress.getLastname());
                    omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                    omsOrderSaveReqDTO.setPostalCode(shippingAddress.getZipCode());
                    omsOrderSaveReqDTO.setState(shippingAddress.getState());
                    omsOrderSaveReqDTO.setAddress1(shippingAddress.getStreet1());
                    omsOrderSaveReqDTO.setAddress2(shippingAddress.getStreet2());
                    omsOrderSaveReqDTO.setBuyerCountryCode(shippingAddress.getCountryIsoCode());
                    omsOrderSaveReqDTO.setRecipientCountryCode(shippingAddress.getCountryIsoCode());
                    omsOrderSaveReqDTO.setCompanyName(shippingAddress.getCompany());
                    omsOrderSaveReqDTO.setPhone(shippingAddress.getPhone());
                    omsOrderSaveReqDTO.setEmail(customer.getCustomerNotificationEmail());
                }
            }


            List<Home24OrderResp.OrderLine> orderLines = order.getOrderLines();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (Home24OrderResp.OrderLine orderLine : orderLines) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
//                omsOrderItemSaveReqDTO.setShopProductId(existShopProductMap.get(lineItem.getVariantId().toString()).getId());
                omsOrderItemSaveReqDTO.setShopProductExternalCode(orderLine.getOfferSku());
                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(orderLine.getPrice()));
                omsOrderItemSaveReqDTO.setQty(orderLine.getQuantity());
                omsOrderItemSaveReqDTO.setExternalId(orderLine.getOrderLineId());
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

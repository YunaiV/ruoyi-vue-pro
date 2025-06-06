package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.bestbuy.resp.BestbuyOrderRespVO;
import com.somle.bestbuy.resp.BestbuyShopRespVO;
import com.somle.bestbuy.service.BestbuyClient;
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
public class BestbuyToOmsConverter {
    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private BestbuyShopRespVO bestbuyShopRespVO;

    private PlatformEnum platform = PlatformEnum.BESTBUY;

    public List<OmsShopSaveReqDTO> toShops(List<BestbuyShopRespVO> bestbuyShopRespVOs) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = bestbuyShopRespVOs.stream().map(bestbuyShopRespVO -> {
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            MapUtils.findAndThen(existShopMap, bestbuyShopRespVO.getShopId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
            shopDTO.setName(null);
            shopDTO.setExternalName(bestbuyShopRespVO.getShopName());
            shopDTO.setCode(null);
            shopDTO.setExternalId(bestbuyShopRespVO.getShopId().toString());
            shopDTO.setPlatformCode(this.platform.toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<BestbuyOrderRespVO.Order> orders, BestbuyClient bestbuyClient) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        if (bestbuyShopRespVO == null) {
            bestbuyShopRespVO = bestbuyClient.getShopInformation();
        }

        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(bestbuyShopRespVO.getShopId().toString());
        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = externalId, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (BestbuyOrderRespVO.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderId(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderId());
            omsOrderSaveReqDTO.setShopId(omsShopDO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getCreatedDate());
            omsOrderSaveReqDTO.setPayTime(order.getCreatedDate());
            if (order.getTotalPrice() != null) {
                omsOrderSaveReqDTO.setTotalPrice(order.getTotalPrice());
            }

            BestbuyOrderRespVO.Customer customer = order.getCustomer();
            if (customer != null) {
                omsOrderSaveReqDTO.setBuyerName(customer.getFirstname() + " " + customer.getLastname());
                BestbuyOrderRespVO.Address shippingAddress = customer.getShippingAddress();
                if (shippingAddress != null) {
                    omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingAddress));
                    omsOrderSaveReqDTO.setRecipientName(customer.getFirstname() + " " + customer.getLastname());
                    omsOrderSaveReqDTO.setRecipientCountryCode(shippingAddress.getCountryIsoCode());
                    omsOrderSaveReqDTO.setState(shippingAddress.getState());
                    omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                    omsOrderSaveReqDTO.setPostalCode(shippingAddress.getZipCode());
                    omsOrderSaveReqDTO.setAddress1(shippingAddress.getStreet1());
                    omsOrderSaveReqDTO.setAddress2(shippingAddress.getStreet2());
                    omsOrderSaveReqDTO.setCompanyName(shippingAddress.getCompany());
                    omsOrderSaveReqDTO.setPhone(shippingAddress.getPhone());
                }
            }


            List<BestbuyOrderRespVO.OrderLine> orderLines = order.getOrderLines();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (BestbuyOrderRespVO.OrderLine orderLine : orderLines) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setQty(orderLine.getQuantity());
                omsOrderItemSaveReqDTO.setPrice(orderLine.getPrice());
                omsOrderItemSaveReqDTO.setExternalId(orderLine.getOrderLineId());
                omsOrderItemSaveReqDTO.setShopProductExternalCode(orderLine.getProductShopSku());
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

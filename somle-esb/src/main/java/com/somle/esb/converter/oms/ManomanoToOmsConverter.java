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
import com.somle.manomano.model.ManomanoShop;
import com.somle.manomano.model.reps.OffersInfoRespVO;
import com.somle.manomano.model.reps.OrderQueryRepsVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class ManomanoToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.MANOMANO;

    public OmsShopSaveReqDTO toShops(ManomanoShop manomanoShop) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));
        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, manomanoShop.getContractId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(manomanoShop.getContractId());
        shopDTO.setCode(null);
        shopDTO.setExternalId(manomanoShop.getContractId());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }


    public List<OmsShopProductSaveReqDTO> toProducts(List<OffersInfoRespVO.ContentDTO> products, ManomanoShop shop) {
        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(shop.getContractId());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = products.stream()
            .map(manomanoProductRepsDTO -> {
                    OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                    MapUtils.findAndThen(existShopProductMap, manomanoProductRepsDTO.getSku() + "#" + omsShopDTO.getId(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                    shopProductDTO.setShopId(omsShopDTO.getId());
                    shopProductDTO.setCode(manomanoProductRepsDTO.getSku());
                    shopProductDTO.setExternalId(manomanoProductRepsDTO.getSku() + "#" + omsShopDTO.getId());
                    shopProductDTO.setName(manomanoProductRepsDTO.getIdMe().toString());
                    if (manomanoProductRepsDTO.getPrice() != null) {
                        shopProductDTO.setPrice(new BigDecimal(manomanoProductRepsDTO.getPrice()));
                    }
                    shopProductDTO.setSellableQty(manomanoProductRepsDTO.getStock());
                    shopProductDTO.setUrl(manomanoProductRepsDTO.getIdMeLink());
                    return shopProductDTO;
                }
            ).toList();
        return omsShopProductDOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<OrderQueryRepsVO.Order> orders, ManomanoShop shop) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));


        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(shop.getContractId());

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (OrderQueryRepsVO.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();

            MapUtils.findAndThen(existOrderMap, order.getSellerContractId().toString() + "#" + order.getOrderReference(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getSellerContractId().toString() + "#" + order.getOrderReference());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(paseTime(order.getCreatedAt()));
            omsOrderSaveReqDTO.setPayTime(paseTime(order.getCreatedAt()));
            omsOrderSaveReqDTO.setTotalPrice(order.getTotalPrice().getAmount());
            if (order.getTotalPrice() != null) {
                omsOrderSaveReqDTO.setTotalPrice(order.getTotalPrice().getAmount());
            }

            OrderQueryRepsVO.Order.Customer customer = order.getCustomer();
            if (customer != null) {
                omsOrderSaveReqDTO.setBuyerName(customer.getFirstname() + " " + customer.getLastname());
            }

            OrderQueryRepsVO.Order.Addresses.Address shippingAddress = order.getAddresses().getShipping();
            if (shippingAddress != null) {
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingAddress));
                omsOrderSaveReqDTO.setRecipientName(shippingAddress.getFirstname() + " " + shippingAddress.getLastname());
                omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                omsOrderSaveReqDTO.setPostalCode(shippingAddress.getZipcode());
                omsOrderSaveReqDTO.setAddress1(shippingAddress.getAddressLine1());
                omsOrderSaveReqDTO.setCompanyName(shippingAddress.getCompany());
                omsOrderSaveReqDTO.setRecipientCountryCode(shippingAddress.getCountryIso());
                omsOrderSaveReqDTO.setPhone(shippingAddress.getPhone());
            }

            if (order.getShippingPrice() != null) {
                omsOrderSaveReqDTO.setShippingFee(order.getShippingPrice().getAmount());
            }
            List<OrderQueryRepsVO.Order.Product> orderProducts = order.getProducts();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (OrderQueryRepsVO.Order.Product product : orderProducts) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setShopProductExternalCode(product.getSellerSku());
                omsOrderItemSaveReqDTO.setExternalId(order.getOrderReference() + "#" + product.getSellerSku());
                omsOrderItemSaveReqDTO.setQty(product.getQuantity());
                omsOrderItemSaveReqDTO.setPrice(product.getProductPrice().getAmount());
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }

    public LocalDateTime paseTime(String time) {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(time);

        LocalDateTime beijingTime = offsetDateTime.toInstant()
            .atZone(ZoneId.of("Asia/Shanghai"))
            .toLocalDateTime();
        return beijingTime;
    }
}

package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.controller.vo.AmazonSpOrderItemRespVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.amazon.model.AmazonSpAuthDO;
import com.somle.amazon.model.reps.AmazonSpListingRepsVO;
import com.somle.esb.enums.PlatformEnum;
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
public class AmazonToOmsConverter {
    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.AMAZON;

    private String AMAZON_URL_PREFIX = "https://www.amazon.com/dp/";

    public List<OmsShopSaveReqDTO> toShops(List<AmazonSpMarketplaceParticipationVO> shopInfoDTOs, AmazonSpAuthDO auth) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            AmazonSpMarketplaceVO marketplace = shopInfoDTO.getMarketplace();
            MapUtils.findAndThen(existShopMap, auth.getSellerId() + '#' + marketplace.getId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
            shopDTO.setName(null);
            shopDTO.setExternalName(shopInfoDTO.getStoreName());
            shopDTO.setCode(null);
            shopDTO.setExternalId(auth.getSellerId() + '#' + marketplace.getId());
            shopDTO.setPlatformCode(this.platform.toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }


    public List<OmsShopProductSaveReqDTO> toProducts(List<AmazonSpListingRepsVO.ProductItem> product, AmazonSpAuthDO auth) {


        Map<String, OmsShopDTO> omsShopDTOMap = omsShopApi.getByPlatformCode(this.platform.toString()).stream()
            .collect(Collectors.toMap(OmsShopDTO::getExternalId, Function.identity()));

        if (MapUtil.isEmpty(omsShopDTOMap)) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<Long> existShopIds = omsShopDTOMap.values().stream().map(OmsShopDTO::getId).toList();
        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(existShopIds);

        List<OmsShopProductSaveReqDTO> omsShopProductDOs = new ArrayList<>();
        for (AmazonSpListingRepsVO.ProductItem amazonProductRepsDTO : product) {
            List<AmazonSpListingRepsVO.ProductSummary> summaryList = amazonProductRepsDTO.getSummaries();
            if (CollectionUtil.isEmpty(summaryList)) {
                continue;
            }
            OmsShopDTO omsShopDTO = omsShopDTOMap.get(amazonProductRepsDTO.getSummaries().get(0).getMarketplaceId() + '#' + auth.getSellerId());
            if (omsShopDTO == null) {
                continue;
            }

            // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
            Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));
            OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
            AmazonSpListingRepsVO.ProductSummary summary = summaryList.get(0);
            MapUtils.findAndThen(existShopProductMap, amazonProductRepsDTO.getSku() + '#' + summary.getAsin() + '#' + summary.getFnSku(),
                omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
            shopProductDTO.setShopId(omsShopDTO.getId());
            shopProductDTO.setCode(amazonProductRepsDTO.getSku());
            shopProductDTO.setName(summary.getItemName());
            shopProductDTO.setExternalId(amazonProductRepsDTO.getSku() + '#' + summary.getAsin() + '#' + summary.getFnSku());
            shopProductDTO.setUrl(AMAZON_URL_PREFIX + summary.getAsin());

            List<AmazonSpListingRepsVO.Offer> offers = amazonProductRepsDTO.getOffers();
            if (CollectionUtil.isNotEmpty(offers)) {
                AmazonSpListingRepsVO.Offer offer = amazonProductRepsDTO.getOffers().get(0);
                AmazonSpListingRepsVO.Offer.Price price = offer.getPrice();
                if (price != null) {
                    shopProductDTO.setPrice(new BigDecimal(price.getAmount()));
                    shopProductDTO.setCurrencyCode(price.getCurrencyCode());
                }
            }

            List<AmazonSpListingRepsVO.FulfillmentAvailability> fulfillmentAvailability = amazonProductRepsDTO.getFulfillmentAvailability();
            if (CollectionUtil.isNotEmpty(fulfillmentAvailability)) {
                shopProductDTO.setSellableQty(fulfillmentAvailability.get(0).getQuantity());
            }
            omsShopProductDOs.add(shopProductDTO);
        }
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<AmazonSpOrderRespVO.Order> orders, AmazonSpAuthDO auth) {
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        // key是platformShopCode
        Map<String, OmsShopDTO> omsShopMap = omsShopApi.getByPlatformCode(this.platform.toString())
            .stream().collect(Collectors.toMap(OmsShopDTO::getExternalId, Function.identity()));

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalCode(), omsOrderDTO -> omsOrderDTO));


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
        for (AmazonSpOrderRespVO.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getAmazonOrderId(), omsOrderDTO -> {
                omsOrderSaveReqDTO.setId(omsOrderDTO.getId());
                omsOrderSaveReqDTO.setCode(omsOrderDTO.getCode());
            });
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalCode(order.getAmazonOrderId());
            MapUtils.findAndThen(omsShopMap, order.getMarketplaceId() + '#' + auth.getSellerId(), omsShopDTO -> omsOrderSaveReqDTO.setShopId(omsShopDTO.getId()));
            omsOrderSaveReqDTO.setPayTime(order.getPurchaseDate());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getPurchaseDate());
            omsOrderSaveReqDTO.setOutboundLatestTime(order.getEarliestShipDate());
            omsOrderSaveReqDTO.setReceiveLatestTime(order.getLatestShipDate());
            AmazonSpOrderRespVO.OrderTotal orderTotal = order.getOrderTotal();
            if (orderTotal != null && orderTotal.getAmount() != null) {
                omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(orderTotal.getAmount()));
            }

            AmazonSpOrderRespVO.BuyerInfo buyerInfo = order.getBuyerInfo();
            if (buyerInfo != null) {
                omsOrderSaveReqDTO.setEmail(buyerInfo.getBuyerEmail());
                omsOrderSaveReqDTO.setBuyerName(buyerInfo.getBuyerName());
                if (buyerInfo.getBuyerTaxInfo() != null) {
                    omsOrderSaveReqDTO.setCompanyName(buyerInfo.getBuyerTaxInfo().getCompanyLegalName());
                }
            }
            AmazonSpOrderRespVO.ShippingAddress shippingAddress = order.getShippingAddress();
            omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingAddress));
            if (shippingAddress != null) {
                omsOrderSaveReqDTO.setAddress1(shippingAddress.getAddressLine1());
                omsOrderSaveReqDTO.setState(shippingAddress.getStateOrRegion());
                omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                omsOrderSaveReqDTO.setPostalCode(shippingAddress.getPostalCode());
                omsOrderSaveReqDTO.setRecipientName(shippingAddress.getName());
                omsOrderSaveReqDTO.setRecipientCountryCode(shippingAddress.getCountryCode());
            }

            List<AmazonSpOrderItemRespVO.OrderItem> orderItems = order.getOrderItems();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (AmazonSpOrderItemRespVO.OrderItem orderItem : orderItems) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                if (ObjectUtil.isNotEmpty(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()))) {
                    omsOrderItemSaveReqDTO.setShopProductId(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()).get(orderItem.getSellerSKU()).getId());
                }
                omsOrderItemSaveReqDTO.setQty(orderItem.getQuantityOrdered());
                if (orderItem.getItemPrice() != null) {
                    omsOrderItemSaveReqDTO.setPrice(new BigDecimal(orderItem.getItemPrice().getAmount()));
                }
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}

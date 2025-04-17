package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.amazon.model.reps.AmazonSpListingRepsVO;
import com.somle.esb.enums.PlatformEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST;

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

    public List<OmsShopSaveReqDTO> toShops(List<AmazonSpMarketplaceParticipationVO> shopInfoDTOs) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getPlatformShopCode(), omsShopDO -> omsShopDO));

        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            AmazonSpMarketplaceVO marketplace = shopInfoDTO.getMarketplace();
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            MapUtils.findAndThen(existShopMap, marketplace.getId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
            shopDTO.setName(null);
            shopDTO.setPlatformShopName(shopInfoDTO.getStoreName());
            shopDTO.setCode(null);
            shopDTO.setPlatformShopCode(marketplace.getId());
            shopDTO.setPlatformCode(this.platform.toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }


    public List<OmsShopProductSaveReqDTO> toProducts(List<AmazonSpListingRepsVO.ProductItem> product, String platformShopCode) {


        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(platformShopCode);

        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }


        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getSourceId(), omsShopProductDO -> omsShopProductDO));

        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream().map(amazonProductRepsDTO -> {
            OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
            MapUtils.findAndThen(existShopProductMap, amazonProductRepsDTO.getSku() + this.platform.toString(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
            shopProductDTO.setShopId(omsShopDO.getId());
            shopProductDTO.setCode(amazonProductRepsDTO.getSku());
            shopProductDTO.setSourceId(amazonProductRepsDTO.getSku() + this.platform.toString());
            if (CollectionUtil.isNotEmpty(amazonProductRepsDTO.getSummaries())) {
                shopProductDTO.setName(amazonProductRepsDTO.getSummaries().get(0).getItemName());
            }
            shopProductDTO.setPrice(null);
            shopProductDTO.setSellableQty(null);
            return shopProductDTO;
        }).toList();
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<AmazonSpOrderRespVO.Order> orders) {
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        // key是platformShopCode
        Map<String, OmsShopDTO> omsShopMap = omsShopApi.getByPlatformCode(this.platform.toString())
            .stream().collect(Collectors.toMap(OmsShopDTO::getPlatformShopCode, Function.identity()));

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
                    MapUtils.findAndThen(existOrderMap, order.getAmazonOrderId(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
                    omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
                    omsOrderSaveReqDTO.setSourceNo(order.getAmazonOrderId());
                    MapUtils.findAndThen(omsShopMap, order.getMarketplaceId(), omsShopDTO -> omsOrderSaveReqDTO.setShopId(omsShopDTO.getId()));
                    omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(Optional.ofNullable(order.getOrderTotal()).map(AmazonSpOrderRespVO.OrderTotal::getAmount).orElse("0.00")));
                    omsOrderSaveReqDTO.setEmail(order.getBuyerInfo().getBuyerEmail());
                    omsOrderSaveReqDTO.setSourceAddress(JsonUtilsX.toJsonString(order.getShippingAddress()));
                    if (order.getShippingAddress() != null) {
                        omsOrderSaveReqDTO.setAddress(order.getShippingAddress().getStateOrRegion());
                    }
                    List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = order.getOrderItems().stream()
                        .map(orderItem -> {
                            OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                            omsOrderItemSaveReqDTO.setShopProductCode(orderItem.getSellerSKU());
                            omsOrderItemSaveReqDTO.setQty(orderItem.getQuantityOrdered());
                            if (orderItem.getItemPrice() != null) {
                                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(orderItem.getItemPrice().getAmount()));
                            }
                            return omsOrderItemSaveReqDTO;
                        }).toList();
                    omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
                    return omsOrderSaveReqDTO;
                } catch (Exception e) {
                    log.info("转换Amazon订单异常", e);
                }
                return null;
            }).toList();
        return omsOrderSaveReqDTOs;
    }
}

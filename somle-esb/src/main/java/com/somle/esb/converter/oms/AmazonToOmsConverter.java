package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.amazon.model.reps.AmazonSpListingRepsVO;
import com.somle.esb.enums.PlatformEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST;

@Component
public class AmazonToOmsConverter {
    @Resource
    OmsShopApi omsShopApi;

    private PlatformEnum platform = PlatformEnum.AMAZON;

    public List<OmsShopSaveReqDTO> toShops(List<AmazonSpMarketplaceParticipationVO> shopInfoDTOs) {
        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            AmazonSpMarketplaceVO marketplace = shopInfoDTO.getMarketplace();
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
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


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getSummaries().stream()
                    .map(summary -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        shopProductDTO.setShopId(omsShopDO.getId());
                        shopProductDTO.setCode(summary.getFnSku());
                        shopProductDTO.setSourceId(omsShopDO.getPlatformShopCode() + summary.getFnSku());
                        shopProductDTO.setName(summary.getItemName());
                        shopProductDTO.setPrice(null);
                        shopProductDTO.setSellableQty(null);
                        return shopProductDTO;
                    })
            ).toList();
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<AmazonSpOrderRespVO.Order> orders) {
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }
//        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = orders.stream()
//            .map(order -> {
//                OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
//                omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
//                omsOrderSaveReqDTO.setSourceNo(order.getAmazonOrderId());
//                omsOrderSaveReqDTO.setShopId(null);
//                omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getOrderTotal().getAmount()));
//                omsOrderSaveReqDTO.setEmail(order.getBuyerInfo().getBuyerEmail());
//                omsOrderSaveReqDTO.setSourceAddress(JsonUtilsX.toJsonString(order.getShippingAddress()));
//            }).toList();
        return null;
    }
}

package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.tiktok.model.resp.TikTokShopResp;
import com.somle.tiktok.sdk.model.Product.V202309.SearchProductsResponseDataProducts;
import com.somle.tiktok.service.TikTokClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;

@Component
@Slf4j
public class TikTokToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;
    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.TIKTOK;

    public List<OmsShopSaveReqDTO> toShops(List<TikTokShopResp.Shop> shops) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        List<OmsShopSaveReqDTO> shopSaveReqDTOS = shops.stream()
            .map(shopInfoDTO -> {
                OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
                MapUtils.findAndThen(existShopMap, shopInfoDTO.getId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
                shopDTO.setName(null);
                shopDTO.setExternalName(shopInfoDTO.getName());
                shopDTO.setCode(null);
                shopDTO.setExternalId(shopInfoDTO.getId().toString());
                shopDTO.setPlatformCode(this.platform.toString());
                shopDTO.setType(ShopTypeEnum.ONLINE.getType());
                return shopDTO;
            })
            .collect(Collectors.toList());

        return shopSaveReqDTOS;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<SearchProductsResponseDataProducts> products, TikTokClient tikTokClient) {
        if (CollUtil.isEmpty(tikTokClient.getShop())) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        TikTokShopResp.Shop shop = tikTokClient.getShop().get(0);

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(shop.getId());

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
            .flatMap(product ->
                product.getSkus().stream()
                    .map(variant -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        MapUtils.findAndThen(existShopProductMap, variant.getId().toString(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                        shopProductDTO.setShopId(omsShopDTO.getId());
                        shopProductDTO.setCode(variant.getSellerSku());
                        shopProductDTO.setExternalId(variant.getId().toString());
                        shopProductDTO.setName(product.getTitle());

                        if (variant.getPrice() != null && variant.getPrice().getTaxExclusivePrice() != null) {
                            shopProductDTO.setPrice(new BigDecimal(variant.getPrice().getTaxExclusivePrice()));
                            shopProductDTO.setCurrencyCode(variant.getPrice().getCurrency());
                        }
                        if (CollUtil.isNotEmpty(variant.getInventory())) {
                            shopProductDTO.setSellableQty(variant.getInventory().get(0).getQuantity());
                        }
                        return shopProductDTO;
                    })
            ).toList();
        return omsShopProductDOs;
    }
}

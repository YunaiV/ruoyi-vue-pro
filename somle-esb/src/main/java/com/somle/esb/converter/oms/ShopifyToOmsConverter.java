package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.enums.shop.PlatformEnum;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.shopify.model.reps.ShopifyShopProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import com.somle.shopify.service.ShopifyClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;


@Component
public class ShopifyToOmsConverter extends AbstractToOmsConverter<ShopifyShopRepsVO, ShopifyShopProductRepsVO> {

    @Resource
    OmsShopApi omsShopApi;

    public ShopifyToOmsConverter() {
        super(PlatformEnum.SHOPIFY);
    }

    @Override
    public List<OmsShopSaveReqDTO> toShop(List<ShopifyShopRepsVO> shopInfoDTOs) {
        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            shopDTO.setName(null);
            shopDTO.setPlatformShopName(shopInfoDTO.getName());
            shopDTO.setCode(null);
            shopDTO.setPlatformShopCode(shopInfoDTO.getId().toString());
            shopDTO.setPlatformCode(this.getPlatform().toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }

    @Override
    public List<OmsShopProductSaveReqDTO> toProduct(List<ShopifyShopProductRepsVO> product, ShopifyClient shopifyClient) {
        if (CollectionUtil.isEmpty(shopifyClient.getShops())) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        ShopifyShopRepsVO shopifyShopRepsVO = shopifyClient.getShops().get(0);



        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());

        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST);
        }


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getVariants().stream()
                    .map(variant -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        shopProductDTO.setShopId(omsShopDO.getId());
                        shopProductDTO.setPlatformCode(variant.getSku());
                        shopProductDTO.setSourceId(variant.getId().toString());
                        shopProductDTO.setName(variant.getTitle());
                        shopProductDTO.setPrice(new BigDecimal(variant.getPrice()));
                        shopProductDTO.setQty(variant.getInventoryQuantity());
                        return shopProductDTO;
                    })
            ).toList();
        return omsShopProductDOs;
    }
}

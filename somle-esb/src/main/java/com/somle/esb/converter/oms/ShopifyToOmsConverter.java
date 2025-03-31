package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import com.somle.esb.client.oms.SyncOmsClient;
import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.model.OmsProfileDTO;
import com.somle.shopify.model.reps.ShopifyProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Component
public class ShopifyToOmsConverter<IN, OUT> extends SalesPlatformToOmsConverter<IN, OUT> {

    @Resource
    OmsShopService omsShopService;

    public ShopifyToOmsConverter() {
        super(SalesPlatform.SHOPIFY);
    }

    @Override
    protected List<OmsShopDO> toOmsShopDO(OmsProfileDTO<IN> shopInfoDTO) {
        List<ShopifyShopRepsVO> shopList = (List<ShopifyShopRepsVO>) shopInfoDTO.getPayload();
        List<OmsShopDO> omsShopDOs = shopList.stream().map(shopifyShopRepsDTO -> {
            OmsShopDO shopDO = new OmsShopDO();
            shopDO.setName(null);
            shopDO.setPlatformShopName(shopifyShopRepsDTO.getName());
            shopDO.setCode(null);
            shopDO.setPlatformShopCode(shopifyShopRepsDTO.getId().toString());
            shopDO.setPlatformCode(shopInfoDTO.getSalesPlatform().name());
            return shopDO;
        }).toList();
        return omsShopDOs;
    }

    @Override
    protected List<OmsShopProductDO> toOmsShopProductDO(OmsProfileDTO<IN> shopProductInfoDTO, SyncOmsClient<?, ?> shopProductProfileClient) {

        //根据client再次请求获取店铺信息
        ShopifyShopRepsVO shopifyShopRepsVO = Optional.ofNullable(shopProductProfileClient)
            .map(SyncOmsClient::getShops)
            .filter(shops -> CollectionUtil.isNotEmpty(shops))
            //由于shopify一个client只对应一个店铺，因此只取集合中的第一个即可
            .map(shops -> shops.get(0))
            .filter(ShopifyShopRepsVO.class::isInstance)
            .map(ShopifyShopRepsVO.class::cast)
            .orElseThrow(() -> new IllegalStateException("无法获取有效的Shopify店铺信息"));

        OmsShopDO omsShopDO = omsShopService.getByPlatformShopCode(shopifyShopRepsVO.getId().toString());

        if (omsShopDO == null) {
            throw new IllegalStateException("请先同步Shopify店铺信息");
        }

        List<ShopifyProductRepsVO> shopProductList = (List<ShopifyProductRepsVO>) shopProductInfoDTO.getPayload();

        List<OmsShopProductDO> omsShopProductDOs = shopProductList.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getVariants().stream()
                    .map(variant -> {
                        OmsShopProductDO shopProductDO = new OmsShopProductDO();
                        shopProductDO.setShopId(omsShopDO.getId());
                        shopProductDO.setPlatformCode(variant.getSku());
                        shopProductDO.setSourceId(variant.getId().toString());
                        shopProductDO.setName(variant.getTitle());
                        shopProductDO.setPrice(new BigDecimal(variant.getPrice()));
                        shopProductDO.setQty(variant.getInventoryQuantity());
                        return shopProductDO;
                    })
            ).toList();
        return omsShopProductDOs;
    }
}

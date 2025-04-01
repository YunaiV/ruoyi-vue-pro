package com.somle.esb.converter.oms;

import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.enums.oms.PlatformEnum;
import com.somle.esb.enums.oms.ShopTypeEnum;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ShopifyToOmsConverter extends AbstractToOmsConverter<ShopifyShopRepsVO> {

    @Resource
    OmsShopApi shopApi;

    public ShopifyToOmsConverter() {
        super(PlatformEnum.SHOPIFY);
    }

    @Override
    public List<OmsShopSaveReqDTO> toShop(List<ShopifyShopRepsVO> shopInfoDTOs) {
        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopInfoDTOs.stream().map(shopInfoDTO -> {
            OmsShopSaveReqDTO shopDO = new OmsShopSaveReqDTO();
            shopDO.setName(null);
            shopDO.setPlatformShopName(shopInfoDTO.getName());
            shopDO.setCode(null);
            shopDO.setPlatformShopCode(shopInfoDTO.getId().toString());
            shopDO.setPlatformCode(this.getPlatform().toString());
            shopDO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDO;
        }).toList();
        return omsShopSaveReqDTOs;
    }
}

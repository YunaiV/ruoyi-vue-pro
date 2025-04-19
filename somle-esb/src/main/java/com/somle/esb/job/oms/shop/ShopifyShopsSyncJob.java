package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.ShopifyToOmsConverter;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ShopifyShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    ShopifyService shopifyService;

    @Resource
    ShopifyToOmsConverter shopifyToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        List<OmsShopSaveReqDTO> shops = shopifyService.shopifyClients.stream().map(client -> {
            OmsShopSaveReqDTO omsShopSaveReqDTO = shopifyToOmsConverter.toShops(client.getShop());
            return omsShopSaveReqDTO;
        }).toList();
        return shops;
    }
}

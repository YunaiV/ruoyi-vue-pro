package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.ShopifyToOmsConverter;
import com.somle.lazada.service.ShopifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class ShopifyShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    ShopifyService shopifyService;

    @Resource
    ShopifyToOmsConverter shopifyToOmsConverter;


    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return shopifyService.shopifyClients.stream()
            .flatMap(
                client -> shopifyToOmsConverter.toProducts(client.getProducts(new HashMap<>()), client).stream()
            )
            .toList();
    }
}

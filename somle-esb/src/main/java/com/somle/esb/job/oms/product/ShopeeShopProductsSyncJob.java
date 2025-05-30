package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.ShopeeToOmsConverter;
import com.somle.shopee.service.ShopeeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ShopeeShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    private ShopeeService shopeeService;

    @Resource
    private ShopeeToOmsConverter shopeeToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return shopeeService.clients.stream()
            .flatMap(client -> shopeeToOmsConverter.toProducts(client.getAllProducts(), client.getAccount()).stream())
            .toList();
    }
}

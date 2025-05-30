package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.ShopeeToOmsConverter;
import com.somle.shopee.service.ShopeeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ShopeeShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    private ShopeeService shopeeService;

    @Resource
    private ShopeeToOmsConverter shopeeToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return shopeeService.clients.stream()
            .flatMap(client -> shopeeToOmsConverter.toShops(List.of(client.getShop()), client.getAccount()).stream())
            .toList();
    }
}

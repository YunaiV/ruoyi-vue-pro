package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.WalmartToOmsConverter;
import com.somle.walmart.service.WalmartService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalmartShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    WalmartService walmartService;
    @Resource
    WalmartToOmsConverter walmartToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return walmartService.walmartClients.stream()
            .filter(client -> client.token.getSvcName().equals("Walmart Marketplace"))
            .map(client -> walmartToOmsConverter.toShops(client.token))
            .toList();
    }
}
